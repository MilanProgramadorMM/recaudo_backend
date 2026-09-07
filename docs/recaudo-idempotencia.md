# Plan de implementación — Idempotencia y control de duplicados en registro de recaudos

Este documento cubre el endpoint `POST /api/recaudo/save` (`RecaudoController.java:39`) y todo lo que
cuelga de él. El objetivo es que **una misma petición de recaudo, enviada más de una vez, produzca un
solo asiento contable** — y que cuando el duplicado no venga de un reintento sino de dos personas o dos
peticiones concurrentes, el sistema lo detecte en vez de descuadrar la cartera en silencio.

> No es un documento de validación como las guías de cartera: aquí se describe **qué construir, en qué
> orden, y por qué cada pieza es necesaria**. Cada fase indica explícitamente qué falla si se omite.

---

## 1. Flujo actual

```
POST /recaudo/save   (multipart: data + file)
  │
  ▼
RecaudoController.processPayment            (RecaudoController.java:39)
  │  · token.substring(7)  → userId, personId   (:46)
  │  · try / catch (Exception) → 400            (:59)
  ▼
NewRecaudoAdapter.processPayment            (NewRecaudoAdapter.java:440)  @Transactional (:439)
  │  · valuePaid > 0
  │  · validateClosingStatus(...)  ←── COMENTADA (:450)
  │
  ├── distributionType = RECAUDO_CAPITAL | RECAUDO_INTERESES
  │     └─ processSingleComponentPayment    (:469)
  │          · 1 fila new_recaudo (quota_id = NULL) + 1 new_recaudo_detail
  │
  └── distributionType = NORMAL | RECAUDO_RUTA | AJUSTE_PERDIDA
        └─ processMultipleQuotasPayment     (:516)
             · lee cuotas pendientes        (:539)
             · loop por cuota               (:551)
             │    · registerDailyVisitIfNotExists
             │    · resolveQuotaComponents  → deuda pendiente de la cuota
             │    · distributePaymentNormal → mora, seg. cartera, seg. vida, interés, capital
             │    · N filas new_recaudo + new_recaudo_detail
             │    · cuota.paid_full / liquidated
             ▼
             procesarRecaudo(...)           (:623)   → dashboard_recaudo  (acumulativo +=)
                └─ descontarDebidoCobrar(...)         → debido_cobrar     (acumulativo -=)
```

**Punto clave:** los dos últimos pasos son **acumulativos**. No escriben un valor, lo suman y lo restan.
Cualquier duplicado no solo mete una fila de más en `new_recaudo`: corrompe permanentemente los agregados
del día, que es lo que la operación mira para cuadrar la ruta.

---

## 2. Qué falla hoy

| # | Problema | Ubicación | Consecuencia |
|---|---|---|---|
| 1 | No existe clave de negocio ni restricción única que impida insertar dos veces el mismo pago | `new_recaudo` | Doble clic o reintento por timeout produce recaudo duplicado y **HTTP 200** |
| 2 | Carrera de concurrencia: la deuda pendiente se lee sin bloqueo | `NewRecaudoAdapter.java:539`, `:551` | Dos peticiones simultáneas aplican el pago completo a la misma cuota: sobrepago y `paid_full` en `S` escrito dos veces |
| 3 | `jakarta.transaction.Transactional` + `throws Exception` | `NewRecaudoAdapter.java:439` | Solo revierte en excepciones *unchecked*. Un `IOException` de `file.getBytes()` a mitad del loop deja **commit parcial** |
| 4 | `procesarRecaudo` recibe `requestDto.getValuePaid()` (solicitado), no lo aplicado | `NewRecaudoAdapter.java:623` | Si el pago excede lo pendiente, el dashboard suma de más y `debido_cobrar` resta de más |
| 5 | `buildRecaudoHeader(requestDto, file, ...)` está **dentro** del loop | `NewRecaudoAdapter.java:574` | El mismo LONGBLOB se persiste una vez por cuota saldada (10 MB por 5 cuotas = 50 MB) |
| 6 | `validateClosingStatus` comentada | `NewRecaudoAdapter.java:450` | Cualquier usuario autenticado puede recaudar sobre cualquier crédito, sin cierre activo |
| 7 | `catch (Exception e)` devuelve 400 con el mensaje interno | `RecaudoController.java:59` | El cliente no puede distinguir "no reintentes" de "reintenta", que es justo lo que necesita para no duplicar. Además anula el `GlobalExceptionHandler` y no loguea nada |
| 8 | `processSingleComponentPayment` no valida que el crédito exista | `NewRecaudoAdapter.java:469` | Recaudo huérfano con `credit_id` inexistente |
| 9 | `token.substring(7)` sin verificar el prefijo | `RecaudoController.java:46` | `StringIndexOutOfBoundsException` convertida en 400 genérico |

Los problemas 3, 4 y 5 no son de idempotencia, pero **agravan** cualquier duplicado y son más baratos de
arreglar que el resto. Por eso van primero en el orden de implementación (§11).

---

## 3. Por qué tres capas y no una

El duplicado tiene tres orígenes distintos y **ninguna capa cubre los tres**. Ese es el argumento central
de este plan:

| Origen del duplicado | Ejemplo | Qué lo ataja |
|---|---|---|
| **Reintento del mismo cliente** | Timeout de red subiendo una foto de 10 MB; el asesor reintenta; la app se reabre y el worker reenvía | Capa A — clave de idempotencia |
| **Concurrencia** | Dos peticiones en vuelo al mismo tiempo sobre el mismo crédito (dos dispositivos, o doble tap que dispara dos requests antes de la primera respuesta) | Capa B — bloqueo pesimista |
| **Mismo pago físico registrado por dos personas** | El cliente muestra el recibo al asesor de ruta y también a la oficina | Capa C — detección de duplicado de negocio |

- Solo con **A**: dos peticiones concurrentes traen claves distintas y legítimas; ambas pasan el filtro y se
  pisan en la misma cuota.
- Solo con **B**: los reintentos se serializan, sí, pero se aplican **los dos**, uno detrás del otro. El
  bloqueo ordena, no deduplica.
- Ni **A** ni **B** detectan **C**: dos personas generan claves distintas y correctas, y sus peticiones
  pueden estar separadas por minutos. Por construcción, ningún esquema de idempotencia lo ve.

Y como red final, independiente del código: una **restricción única en la base de datos** (§5). Si un bug
futuro salta las tres capas, la BD rechaza la fila. Una invariante contable no debería depender de que el
código de aplicación esté correcto.

---

## 4. Decisiones de diseño

### 4.1 La clave la genera el cliente, es aleatoria y opaca

`Idempotency-Key: <UUID v4>` en el header. **No se deriva del contenido.**

**Por qué no un hash del payload.** Es la alternativa que primero se ocurre —`SHA-256(creditId + valuePaid +
fecha + usuario)`— y es incorrecta en este dominio. En recaudo de ruta es perfectamente legítimo que el
mismo asesor registre **dos pagos idénticos al mismo crédito el mismo día**: el cliente abona dos veces, o
se cobran dos cuotas por separado con el mismo valor. Una clave determinista rechazaría el segundo pago
real como si fuera duplicado. Ese falso positivo es **peor** que el duplicado que se quiere evitar: se
pierde plata efectivamente recibida, y el asesor cuadra caja con un faltante que nadie puede explicar.

La clave identifica **el intento de envío**, no el contenido.

**Por qué no la genera el backend.** El propósito es que un reintento sea reconocible. Si el cliente nunca
recibió la respuesta —que es exactamente el caso a cubrir— no tiene la clave que el backend generó, y el
reintento sería indistinguible de un pago nuevo.

### 4.2 La restricción única incluye `username`

`UNIQUE (idempotency_key, endpoint, username)`. Dos asesores distintos nunca pueden bloquearse entre sí, ni
siquiera ante una colisión de UUID (astronómicamente improbable, pero la columna extra es gratis).

### 4.3 El registro de idempotencia vive en su propia transacción

`REQUIRES_NEW`. Si el pago falla y revierte, el registro de la clave **debe sobrevivir** para poder decirle
al cliente qué pasó en el reintento. Si viviera en la misma transacción, el rollback lo borraría y el
reintento se procesaría como si fuera nuevo.

### 4.4 La detección de duplicado de negocio es confirmación, no bloqueo

Rechazar de plano un pago "parecido" perdería pagos reales: el caso C de §3 es ambiguo por naturaleza —puede
ser un duplicado o dos abonos legítimos—. El backend responde 409 con el detalle del pago similar (quién,
cuándo, cuánto) y el usuario decide. La confirmación queda auditada.

### 4.5 Sin Flyway ni Liquibase

El proyecto usa `ddl-auto: none` (`application.yml`) y no tiene herramienta de migración. Los DDL de este
plan van como script versionado en `src/main/resources/db/` y se aplican a mano, documentando fecha y
ambiente de aplicación. Introducir Flyway es recomendable pero **es un cambio separable**: no lo mezcles
con esta entrega.

---

## 5. Fase 1 — Esquema

Archivo: `src/main/resources/db/2026-09-05__idempotencia_recaudo.sql`

### 5.1 Tabla de claves

```sql
CREATE TABLE idempotency_record (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  idempotency_key     VARCHAR(64)  NOT NULL,
  endpoint            VARCHAR(120) NOT NULL,
  username            VARCHAR(255) NOT NULL,
  request_fingerprint CHAR(64)     NOT NULL,
  status              VARCHAR(20)  NOT NULL,   -- IN_PROGRESS | COMPLETED | FAILED
  http_status         INT          NULL,
  response_body       TEXT         NULL,
  created_at          DATETIME     NOT NULL,
  completed_at        DATETIME     NULL,
  expires_at          DATETIME     NOT NULL,
  CONSTRAINT uk_idem UNIQUE (idempotency_key, endpoint, username),
  INDEX idx_idem_expires (expires_at)
) ENGINE=InnoDB;
```

`endpoint` está en la clave para poder reusar el mismo mecanismo en `/recaudo/reverse/*` más adelante sin
que las claves de un endpoint interfieran con las de otro.

### 5.2 Red final en la tabla de recaudos

```sql
ALTER TABLE new_recaudo ADD COLUMN idempotency_key VARCHAR(64) NULL;
ALTER TABLE new_recaudo ADD COLUMN quota_key BIGINT AS (COALESCE(quota_id, 0)) STORED;
CREATE UNIQUE INDEX uk_new_recaudo_idem ON new_recaudo (idempotency_key, quota_key);
```

Dos detalles de MySQL que hay que entender o el índice no protege lo que parece:

1. **Los `NULL` no colisionan en un índice único.** Es lo que hace seguro el `ALTER` sobre una tabla con
   histórico: todas las filas viejas tienen `idempotency_key = NULL` y ninguna choca con otra.
2. **Por lo mismo hace falta `quota_key`.** `processSingleComponentPayment` guarda `quota_id = NULL`
   (`NewRecaudoAdapter.java:469`); sin la columna generada, dos filas con la misma clave y `quota_id` nulo
   pasarían el índice sin problema, que es precisamente el caso que se quiere bloquear.

**Por qué la clave es `(idempotency_key, quota_key)` y no solo `idempotency_key`:** una sola petición de
`processMultipleQuotasPayment` genera **N filas**, una por cuota saldada. Todas comparten clave y se
distinguen por cuota. Con un índice solo sobre la clave, la segunda cuota del mismo pago fallaría.

### 5.3 Índice de apoyo para la detección de duplicado de negocio

```sql
CREATE INDEX idx_new_recaudo_dup ON new_recaudo (credit_id, created_at);
```

### 5.4 Verificación tras aplicar

```sql
SHOW INDEX FROM new_recaudo WHERE Key_name IN ('uk_new_recaudo_idem', 'idx_new_recaudo_dup');
SELECT COUNT(*) FROM new_recaudo WHERE idempotency_key IS NOT NULL;  -- debe dar 0 antes del despliegue
```

---

## 6. Fase 2 — `IdempotencyService`

### 6.1 Por qué no un filtro ni un interceptor

La opción "elegante" sería un `HandlerInterceptor` genérico. No sirve aquí: el endpoint es
`multipart/form-data` con un archivo de hasta 10 MB, y calcular la huella en el interceptor obliga a
bufferizar y releer el cuerpo completo antes de que Spring lo parsee. Es caro y frágil.

La solución es un servicio invocado explícitamente desde el controller, después de que Spring ya deserializó
las partes.

### 6.2 Forma

```java
RecaudoResultDto result = idempotencyService.execute(
        key,                 // header Idempotency-Key
        ENDPOINT_SAVE,
        username,
        fingerprint,         // ver 6.3
        () -> recaudoAdapter.processPayment(request, fileBytes, personId, token)
);
```

### 6.3 Cálculo de la huella (`request_fingerprint`)

`SHA-256` sobre la concatenación canónica de:

```
creditId | valuePaid.setScale(2) | paymentTypeId | bankId | accountNumber | distributionType | username
```

**Qué NO va en la huella, y por qué:**

- **`confirmarDuplicado`** (§9). Si entrara, el reintento confirmado tras un 409 tendría payload distinto y
  el backend respondería 422 en vez de procesar. Es el error más fácil de cometer en esta fase.
- **Los bytes del archivo.** En el plan original iban incluidos; revísalo con el equipo móvil antes de
  implementarlo. Varios selectores de imagen re-comprimen el archivo cada vez que se lee, así que un
  reintento puede producir bytes distintos y disparar un **422 espurio sobre un pago legítimo**. Si no se
  puede garantizar que el archivo sea byte-idéntico entre reintentos, usa solo `filename + size`, o
  exclúyelo por completo: los campos monetarios ya bastan para detectar la reutilización peligrosa de una
  clave.

### 6.4 Máquina de estados

```
   ┌─────────────────────────────────────────────────────────────┐
   │ INSERT idempotency_record (status = IN_PROGRESS)            │
   │   … en transacción REQUIRES_NEW                             │
   └───────────────┬───────────────────────┬─────────────────────┘
                   │ éxito                 │ DuplicateKeyException
                   ▼                       ▼
        ejecutar processPayment    leer el registro existente
                   │                       │
        ┌──────────┴──────────┐            ├─ fingerprint distinto ──→ 422
        ▼                     ▼            ├─ IN_PROGRESS ───────────→ 409 (en vuelo)
   COMPLETED              FAILED           ├─ COMPLETED ─────────────→ 200 + response_body
   + http_status          (reprocesable)   │                            + Idempotent-Replay: true
   + response_body                         └─ FAILED ────────────────→ reprocesar
```

Ambas transiciones finales (`COMPLETED` / `FAILED`) se escriben en transacción independiente, por §4.3.

### 6.5 Periodo de gracia

Durante N semanas, si falta el header `Idempotency-Key` solo se registra `log.warn` y la petición pasa.
Cumplido el plazo, se responde 400. Así el backend se despliega **antes** que la app móvil sin romper a los
clientes en producción, que es la única forma realista de sacar esto en un sistema ya en uso.

---

## 7. Fase 3 — Bloqueo pesimista (concurrencia)

### 7.1 Repositorio

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select c from CreditEntity c where c.id = :id")
Optional<CreditEntity> findByIdForUpdate(@Param("id") Long id);
```

### 7.2 Uso — primera sentencia de `processPayment`

```java
@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public RecaudoResultDto processPayment(...) throws Exception {

    creditRepository.findByIdForUpdate(requestDto.getCreditId())
        .orElseThrow(() -> new ResourceNotFoundException("Crédito no encontrado"));
    ...
}
```

### 7.3 Los tres detalles que hacen que funcione

1. **El lock va antes de leer las cuotas.** Si se toma después de
   `findByCreditIdAndPaidFullOrderByQuotaNumberAsc` (`:539`), la lista de pendientes ya viene rancia y el
   bloqueo no sirve de nada.
2. **`isolation = READ_COMMITTED`.** MySQL usa REPEATABLE READ por defecto, donde el snapshot se fija en la
   primera lectura consistente y el comportamiento de las lecturas posteriores a un *locking read* es sutil
   y dependiente de versión. Con READ_COMMITTED no hay ambigüedad: el segundo pago lee el estado ya
   confirmado por el primero.
3. **Fija un lock timeout explícito.** El pool de Hikari es de 10 conexiones (`application.yml`) y el
   `innodb_lock_wait_timeout` por defecto es 50 s. Bajo contención es mejor fallar rápido con un 409 que
   dejar conexiones colgadas y agotar el pool.

### 7.4 Por qué el segundo pago hace lo correcto sin código nuevo

`processMultipleQuotasPayment` ya lo contempla. Cuando el segundo pago obtiene el lock y lee el estado
fresco, o bien la cuota saldada ya no aparece en `cuotasPendientes`, o bien cae en el
`if (comp.totalPending() <= 0)` de `NewRecaudoAdapter.java:560` y hace `continue` a la siguiente. El dinero
se aplica a la cuota siguiente y, si no quedan cuotas, vuelve como `saldoSobrante` en el `RecaudoResultDto`.

**Requisito de UI:** la app del segundo asesor debe **mostrar** ese resultado ("se aplicó a la cuota N+1",
"quedó $X a favor"). Si no, el asesor ve un 200 y asume que pagó la cuota que tenía en pantalla.

### 7.5 Beneficio lateral

Ese `findByIdForUpdate` valida de paso que el crédito exista, lo que cierra el problema 8 de §2 en el camino
`RECAUDO_CAPITAL` / `RECAUDO_INTERESES`, que hoy no valida nada.

---

## 8. Fase 4 — Transaccionalidad y bugs de cuadre

Estos cambios son independientes de la idempotencia y **deberían ir primero**: hoy hay riesgo de commit
parcial en producción.

| Cambio | Ubicación | Por qué |
|---|---|---|
| Usar `org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)` | `NewRecaudoAdapter.java:439` | La anotación de `jakarta.transaction` no revierte en excepciones checked, y el método declara `throws Exception` |
| Leer `file.getBytes()` **una sola vez**, antes del loop y antes de abrir la transacción | `NewRecaudoAdapter.java:574` | Elimina el `IOException` a mitad de transacción y de paso la duplicación del blob por cuota (problema 5) |
| Pasar a `procesarRecaudo` el total realmente aplicado (`valuePaid - saldoRestante`) | `NewRecaudoAdapter.java:623` | Hoy el dashboard y `debido_cobrar` se mueven por el valor solicitado, no el aplicado (problema 4) |
| Quitar el `catch (Exception e)` y dejar actuar al `GlobalExceptionHandler` | `RecaudoController.java:59` | El cliente necesita distinguir 4xx (no reintentar) de 5xx (reintentar). Sin eso, la política de reintentos del front es adivinanza |
| Validar el prefijo `Bearer ` antes del `substring(7)` | `RecaudoController.java:46` | Problema 9 |
| Descomentar `validateClosingStatus` | `NewRecaudoAdapter.java:450` | Ver §10 |

---

## 9. Fase 5 — Duplicado de negocio (dos usuarios, misma cuota)

### 9.1 Qué problema resuelve exactamente

El caso C de §3: **el mismo pago físico registrado por dos personas**. Las claves de idempotencia son
distintas y legítimas, el bloqueo pesimista las serializa correctamente, y aun así hay dos asientos por un
solo pago real. Es el único caso que requiere lógica de negocio.

### 9.2 Implementación

```java
// ya con el lock del crédito tomado, antes de aplicar el pago
var similares = recaudoRepository.findPosiblesDuplicados(
        creditId,
        valuePaid.negate(),                          // en BD se guarda negado
        LocalDateTime.now().minusMinutes(ventanaMin));

if (!similares.isEmpty() && !Boolean.TRUE.equals(dto.getConfirmarDuplicado())) {
    throw new PosibleDuplicadoException(similares);   // → 409 con el detalle
}
```

El 409 debe devolver, por cada pago similar: `recaudoId`, `userCreate`, `createdAt` y `valuePaid`, para que
el segundo asesor decida con información. La confirmación se registra en log de auditoría (quién la forzó y
sobre qué recaudo).

### 9.3 Elegir la ventana

| Ventana | Atrapa | Cuesta |
|---|---|---|
| 15–30 min | El caso "mismo recibo, dos personas", que es el real | Muy pocos falsos positivos |
| Todo el día | También duplicados diferidos | Molesta a los clientes que sí abonan dos veces al día |

**Recomendación:** arrancar en 15 min, configurable en `application.yml`, y subirla con datos reales de
producción.

### 9.4 Interacción crítica con la Fase 2

`confirmarDuplicado` **no entra en el `request_fingerprint`** (§6.3). El cliente reenvía con la **misma**
clave de idempotencia y el flag en `true`; el registro quedó en `FAILED`, que según §6.4 permite reprocesar.
Si el flag entrara en la huella, el reenvío confirmado devolvería 422 y el pago no se registraría nunca.

---

## 10. Fase 6 — Control previo: `validateClosingStatus`

Descomentar `NewRecaudoAdapter.java:450`. Esa validación exige cierre activo del asesor en la zona del
crédito — es decir, existe **precisamente** para que el escenario de dos usuarios sobre la misma cuota sea
raro en primer lugar. Hoy está desactivada y cualquier usuario autenticado puede recaudar sobre cualquier
crédito.

Es la causa de fondo del caso C, y arreglarla reduce el volumen de 409 de la Fase 5 mucho más que cualquier
ajuste de ventana. Antes de activarla, confirma con operaciones que no rompa un flujo legítimo actual (por
ejemplo, recaudos hechos desde oficina sin cierre de ruta).

---

## 11. Fase 7 — Limpieza y observabilidad

- **Job de purga** en el scheduler ya existente (`app.scheduler.cron`, `application-scheduler.yml`): borrar
  `idempotency_record` con `expires_at < now()`. Retención sugerida: 24–72 h, la operación es de ruta diaria.
- **Métricas / logs**: contador de replays (200 con `Idempotent-Replay`), de 409 en vuelo, de 409 por
  duplicado de negocio y de 422 por huella distinta.
- **Alerta**: si los replays suben de golpe, hay un problema de red o de UI que vale la pena mirar. Si suben
  los 422, el front está regenerando claves cuando no debe (o el archivo entró en la huella, §6.3).

---

## 12. Fase 8 — Pruebas

| # | Escenario | Resultado esperado |
|---|---|---|
| 1 | Doble POST con la misma clave y mismo payload | Un solo `new_recaudo`; la segunda respuesta es idéntica y trae `Idempotent-Replay: true` |
| 2 | Misma clave, payload distinto | 422; ninguna fila nueva |
| 3 | Dos hilos concurrentes con la misma clave | Uno gana; el otro recibe 409 o replay. Nunca dos asientos |
| 4 | Dos hilos, claves distintas, mismo crédito y misma cuota | El lock serializa; el segundo pago rueda a la cuota siguiente o vuelve como `saldoSobrante`. Sin sobrepago |
| 5 | Forzar `IOException` al leer el archivo | Cero filas en `new_recaudo`, `new_recaudo_detail`, `dashboard_recaudo` y `debido_cobrar` |
| 6 | Insertar a mano dos filas con misma clave y misma cuota | La BD rechaza por `uk_new_recaudo_idem` |
| 7 | Pago que excede lo pendiente | `dashboard_recaudo` y `debido_cobrar` se mueven por lo **aplicado**, no por lo solicitado |
| 8 | Dos abonos legítimos del mismo asesor, misma cuota, mismo día, valores iguales | Ambos se registran (tras confirmar el 409 de §9). **Este test protege contra el falso positivo de §4.1** |
| 9 | Petición sin header durante el periodo de gracia | Se procesa, con `log.warn` |

El test 8 es el más importante del conjunto: es el que impide que alguien "optimice" el diseño hacia una
clave determinista en el futuro.

---

## 13. Orden de implementación

| Orden | Fase | Esfuerzo | Rompe clientes |
|---|---|---|---|
| 1 | **Fase 4** — transaccionalidad y cuadre (§8) | Bajo | No |
| 2 | **Fase 1** — esquema (§5) | Bajo | No |
| 3 | **Fase 2** — `IdempotencyService` con periodo de gracia (§6) | Medio | No (gracia) |
| 4 | **Fase 3** — bloqueo pesimista (§7) | Bajo | No |
| 5 | **Fase 5** — contrato con el front: ver `docs/` del cliente | Medio | Requiere despliegue móvil |
| 6 | **Fase 6** — `validateClosingStatus` (§10) | Bajo | Sí — validar con operaciones antes |
| 7 | **Fase 5 backend** — duplicado de negocio (§9) | Medio | No (flag opcional) |
| 8 | **Fases 7 y 8** — purga, métricas, pruebas (§11, §12) | Medio | No |

La Fase 4 va primero porque el riesgo de commit parcial **ya existe en producción** y su arreglo no depende
de nada más. Las fases 1 a 4 son invisibles para los clientes actuales gracias al periodo de gracia.

---

## 14. Contrato con el cliente (resumen)

El detalle está en la conversación de diseño; lo esencial:

- La clave se **genera al crear la intención de pago** (al abrir el formulario limpio, o al encolar el pago
  offline), **nunca dentro del `onSubmit`**. Si se genera al enviar, cada reintento trae clave nueva y todo
  el mecanismo queda decorativo.
- Se **persiste** junto al pago pendiente (SQLite / AsyncStorage / IndexedDB), para que sobreviva al cierre
  de la app. Si la app muere entre el envío y la respuesta, el reintento al reabrir debe traer la misma clave.
- Se **reusa** en cada reintento: timeout, 5xx, conexión caída, app reabierta.
- Se **regenera** cuando el usuario edita el payload tras un fallo de validación.
- Se **descarta** ante 200 o 4xx definitivo. Formulario nuevo, clave nueva.

Manejo de respuestas:

| Código | Acción del cliente |
|---|---|
| 200 | Éxito. Si trae `Idempotent-Replay: true`, era un reintento de algo ya aplicado: no volver a sumar nada en pantalla |
| 409 en vuelo | Esperar y reintentar con la **misma** clave |
| 409 posible duplicado | Mostrar el detalle del pago similar; si el usuario confirma, reenviar con la **misma** clave y `confirmarDuplicado = true` |
| 422 | Clave reusada con otro payload: bug del cliente. Loguear, generar clave nueva, reenviar |
| 400 de validación | El usuario corrige y se envía con clave **nueva** |
| 5xx / timeout / red caída | Reintentar con la **misma** clave, con backoff |

Nota de implementación: `@RequestPart("data")` exige que esa parte del multipart venga con
`Content-Type: application/json`; un `FormData` que la mande como texto plano falla con 415.

---

## 15. Limitaciones conocidas

1. **`/recaudo/reverse/*` queda fuera de alcance.** `reverseRecaudos` (`NewRecaudoAdapter.java:671`) tiene
   exactamente el mismo problema: reversar dos veces el mismo recaudo no está bloqueado. El
   `IdempotencyService` de la Fase 2 le aplica tal cual — por eso `endpoint` está en la clave única (§5.1).
2. **La detección de duplicado de negocio es heurística.** Una ventana de tiempo y un valor igual no prueban
   que sea el mismo pago. Por eso es confirmación y no bloqueo (§4.4).
3. **La idempotencia no cubre el error humano de digitación.** Si el asesor registra $50.000 en vez de
   $500.000, ninguna de las tres capas lo detecta; eso lo resuelve la reversión, no este plan.
4. **El periodo de gracia es una ventana de riesgo real.** Mientras esté activo, un cliente que no mande el
   header sigue pudiendo duplicar. Define la fecha de corte al iniciar la Fase 2 y no la dejes abierta.
