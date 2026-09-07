# 001 — Diagnóstico: valores inflados en `dashboard_recaudo` (caso cierre 902, zona ELITE)

**Fecha del análisis:** 2026-09-06
**Caso reportado:** el cierre 902 muestra valores muy superiores a lo realmente recaudado.
**Estado:** diagnóstico cerrado, causa confirmada. **Ninguna corrección aplicada todavía.**

---

## 1. El caso

`dashboard_recaudo`, zona 4 (ELITE), 2026-09-05:

| id | value | zona_id | zona_code | user_create | created_at | create_recaudo |
|---|---|---|---|---|---|---|
| 2032 | **2.458.500** | 4 | ELITE | EIDERSAMIA | 2026-09-05 14:37:35 | 2026-09-05 |

`new_recaudo` de esa zona y día:

| id | credit_id | quota_id | value_paid | user_create | created_at |
|---|---|---|---|---|---|
| 45501 | 705 | 35016 | −700.000,00 | EIDERSAMIA | 2026-09-05 14:38:01 |
| 45500 | 748 | 35989 | −555.500,00 | EIDERSAMIA | 2026-09-05 14:37:35 |

Recaudado real: **$1.255.500**. Dashboard: **$2.458.500**. **Exceso: $1.203.000** sin ninguna
contrapartida contable.

---

## 2. De dónde sale cada cifra

```
POST /recaudo/save
  └─ NewRecaudoAdapter.processMultipleQuotasPayment          (NewRecaudoAdapter.java:516)
       ├─ loop por cuota pendiente                            (:551)
       │    ├─ registerDailyVisitIfNotExists  ← rastro en collection_visit
       │    ├─ if (comp.totalPending() <= 0)  continue        (:560)  ← marca la cuota como pagada
       │    ├─ dist = distributePaymentNormal(comp, saldoRestante)
       │    ├─ if (dist.getTotalApplied() <= 0) continue      (:570)
       │    └─ INSERT new_recaudo (valor APLICADO)            (:574)
       │
       └─ procesarRecaudo(zonaId, zonaCode, requestDto.getValuePaid())   (:623)  ← ⚠️
            ├─ dashboard_recaudo.value += valorAbonado        (:661)
            └─ descontarDebidoCobrar(zonaId, valorAbonado)    (:665)
```

La cifra del cierre se lee después con
`DashboardMetricsRepository.getTotalRecaudado(fechaInicio, fechaFin, zonaId)`
(`DashboardMetricsRepository.java:26`), que filtra **solo por `zona_id` y rango de fechas**.

**El invariante que debería cumplirse siempre:**

```
dashboard_recaudo.value  ==  SUM(-new_recaudo.value_paid)   -- misma zona, mismo día
```

Hoy el lado izquierdo es mayor.

---

## 3. Causas evaluadas

### 3.1 Causa 1 — agregación por zona vs. cierre por asesor — **DESCARTADA en este caso, latente**

`findByZonaIdAndCreateRecaudo(zonaId, hoy)` devuelve **una sola fila por zona y día**.
`procesarRecaudo` la crea la primera vez con `userCreate = getUsernameToken()` y después solo hace
`record.setValue(record.getValue().add(valorAbonado))` — **`user_create` nunca se actualiza**. Por eso
la fila 2032 dice `EIDERSAMIA`: es simplemente quien recaudó primero a las 14:37:35, no el único
aportante.

Pero el cierre es por `(person_id, zona_id, closing_date)` — ver
`existsByPersonIdAndZonaIdAndClosingDate` y `findTodayClosingByPersonAndZona` en `ClosingRepository`.
Leer un acumulador de zona para un cierre de asesor es un error de categoría.

**Descartada aquí** porque ELITE tiene un solo asesor. Verificado:

```sql
SELECT nr.user_create, COUNT(*) AS n, SUM(-nr.value_paid) AS total
FROM new_recaudo nr
JOIN credit c            ON c.id  = nr.credit_id
JOIN credit_intention ci ON ci.id = c.credit_intention_id
WHERE ci.zone_id = 4 AND DATE(nr.created_at) = '2026-09-05' AND nr.value_paid < 0
GROUP BY nr.user_create WITH ROLLUP;
```

Resultado: `EIDERSAMIA | 2 | 1255500.00` y ROLLUP `1255500.00`. Un solo asesor, dos recaudos.

⚠️ **Sigue latente:** el día que dos asesores compartan una zona, ambos cierres mostrarán la suma de
los dos.

### 3.2 Causa 2 — el dashboard suma lo SOLICITADO, no lo APLICADO — **CONFIRMADA**

```java
procesarRecaudo(dataZona.getId(), dataZona.getCode(), requestDto.getValuePaid());  // :623
```

Está **fuera del loop** y usa el valor del request. Se ejecuta **siempre** que el método llegue al
final, sin importar cuánto se aplicó — incluso si el loop no creó ninguna fila.

`new_recaudo` guarda `dist.getTotalApplied()`; el dashboard recibe `requestDto.getValuePaid()`. La
diferencia es `saldoRestante`, que se devuelve al cliente como `saldoSobrante` y aun así entra al
acumulado.

Como `descontarDebidoCobrar` se llama **dentro** de `procesarRecaudo` (`:665`), el `debido_cobrar` de
la zona arrastra exactamente el mismo error.

### 3.3 Causa 3 — cuotas iniciales de toda la zona en el listado del cierre — **latente, no evaluada**

En el `UNION ALL` de `findRecaudosWithClientName` (`NewRecaudoRepository`):

```sql
WHERE DATE(c.created_at) = :fecha
  AND c.initial_value_payment > 0
  AND z.id = :zonaId
```

El primer bloque de la query sí filtra por el asesor del cierre
(`cl.id = :closingId` → person → user → `nr.user_create`), pero este segundo bloque **no filtra por
cierre ni por asesor**. Cualquier cuota inicial de un crédito creado ese día en la zona aparece en el
cierre, sin importar quién lo originó.

---

## 4. Causa 2 en profundidad: son tres escenarios distintos

La línea 623 admite tres historias, y **cada una deja rastros distintos**:

| Escenario | ¿Fila en `new_recaudo`? | ¿Rastro en `collection_visit`? | ¿En logs? |
|---|---|---|---|
| **S1** — el asesor digitó de más sobre un crédito que sí tenía deuda | Sí, con lo aplicado | Visita normal | `saldoSobrante > 0` |
| **S2** — reintento de una petición ya aplicada | **Ninguna** | **Ninguna** (la visita ya existía) | `saldoSobrante` = monto completo |
| **S3** — pago sobre un crédito sin deuda pendiente | **Ninguna** | **Visita nueva sin recaudo** | `saldoSobrante` = monto completo |

**S2 y S3 no dejan ninguna fila en `new_recaudo`.** El dashboard sube y la cartera no se entera. Por
eso la diferencia no cuadra contra ninguna tabla de movimientos.

**S2 merece atención especial:** es exactamente un reintento por timeout de red. El sistema *parece*
idempotente —no duplica el recaudo, porque la cuota ya está cubierta y cae en el `continue` de
`:560`— pero el dashboard sí lo cuenta dos veces. Es el mismo problema descrito en
[`recaudo-idempotencia.md`](./recaudo-idempotencia.md), manifestándose en el único punto sin
protección: el acumulador.

### 4.1 Un segundo mecanismo, independiente del error de digitación

Se puede perder dinero **aunque el asesor digite el monto exacto**. El loop decide cuánto aplicar con
dos medidas que no están garantizadas a coincidir:

- `comp.totalPending()` usa `totalDebt()` = **`totalQuotaValue`** (de `credit_amortization`)
  **+ mora causada**
- `distributePaymentNormal` reparte sobre los **componentes individuales** (`pendingCapital`,
  `pendingInterest`, `pendingLifeInsurance`, `pendingPortfolioInsurance`, `pendingMora`), que salen de
  `credit_amortization_detail`

Ver `QuotaComponentsDto`: `totalDebt()` parte de `totalQuotaValue`, mientras que los `pendingX()`
parten de los cuatro componentes del detalle.

Si `total_quota_value` **no** es igual a la suma de sus cuatro detalles (conceptos 48, 49, 50, 51),
las medidas divergen. Cuando `total_quota_value` es **mayor**, el loop ve deuda que no puede colocar
en ningún componente: aplica menos de lo pendiente, el resto queda en `saldoRestante`, y termina
sumado al dashboard.

Verificación sobre las cuotas de este caso:

```sql
SELECT ca.id, ca.quota_number, ca.total_quota_value,
       SUM(cad.value) AS suma_detalles,
       ca.total_quota_value - SUM(cad.value) AS diferencia
FROM credit_amortization ca
JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
WHERE ca.id IN (35016, 35989)
  AND cad.concept_id IN (48, 49, 50, 51)
GROUP BY ca.id, ca.quota_number, ca.total_quota_value;
```

Si `diferencia <> 0`, este mecanismo está activo y el problema es estructural, no de digitación.

### 4.2 Efecto colateral: cuotas marcadas como pagadas sin recaudo

El `continue` de `:560` no solo salta la cuota — antes la marca:

```java
cuota.setLiquidated("S");
cuota.setPaidFull("S");
amortizationRepository.save(cuota);
```

Una petición con exceso **recorre todas las cuotas pendientes del crédito** y va marcando como
totalmente pagadas las que encuentre con `totalPending() <= 0`, sin generar recaudo alguno. Si esas
cuotas estaban en `'N'` por un desfase de datos, quedan en `'S'` sin contrapartida contable.

### 4.3 Asimetría en sentido contrario

`processSingleComponentPayment` (`:469`), la ruta de `RECAUDO_CAPITAL` y `RECAUDO_INTERESES`,
**nunca llama a `procesarRecaudo`**. Esos pagos crean fila en `new_recaudo` pero no suman al
dashboard. El error va en los dos sentidos: unas veces infla, otras subestima.

---

## 5. Cómo determinar cuál escenario ocurrió

### 5.1 `collection_visit` — único rastro persistido de S3

Cada petición registra visita por cada cuota que recorre, **antes** de cualquier `continue`
(`registerDailyVisitIfNotExists`, `:554`). Un crédito visitado sin recaudo ese día es una petición
que no aplicó nada:

```sql
SELECT cv.credit_id, cv.cuota_id, cv.advisor_username, cv.paid
FROM collection_visit cv
JOIN credit c            ON c.id  = cv.credit_id
JOIN credit_intention ci ON ci.id = c.credit_intention_id
WHERE ci.zone_id = 4
  AND cv.visit_date = '2026-09-05'
  AND NOT EXISTS (
      SELECT 1 FROM new_recaudo nr
      WHERE nr.quota_id = cv.cuota_id AND DATE(nr.created_at) = '2026-09-05'
  );
```

- Aparecen créditos distintos de 705 y 748 → **S3**.
- Solo aparecen esos dos → **S1** o **S2**.

⚠️ Limitación: `registerDailyVisitIfNotExists` no hace nada si ya existe visita para esa cuota y
fecha. Por eso **S2 es invisible** también en esta tabla.

### 5.2 Logs — única fuente que cubre los tres escenarios

`NewRecaudoAdapter.java:626` registra:

```
Pago NORMAL procesado. cuotasLiquidadas=..., saldoSobrante=...
```

Buscar `saldoSobrante` el 2026-09-05 entre 14:37 y 14:39:

- un `saldoSobrante` de 1.203.000 → **S1**
- un `saldoSobrante` de exactamente 700.000 o 555.500 → **S2**, un reintento

⚠️ **El valor solicitado no se persiste en ninguna tabla.** `new_recaudo` guarda solo lo aplicado y
no hay columna que registre lo pedido. Los logs son la única evidencia post-mortem.

### 5.3 Perseguir la cifra

$1.203.000 no es un número redondo de digitación (no es 1.200.000), así que probablemente corresponde
a algo real: el pendiente de un crédito o el valor de una cuota. Buscarlo entre las cuotas de ELITE
puede señalar directamente el crédito involucrado.

---

## 6. Alcance: cuántas zonas y días están afectados

```sql
SELECT dr.id, dr.zona_id, dr.zon_code, dr.create_recaudo,
       dr.value AS dashboard,
       COALESCE(r.real_recaudado, 0) AS real_recaudado,
       dr.value - COALESCE(r.real_recaudado, 0) AS diferencia
FROM dashboard_recaudo dr
LEFT JOIN (
    SELECT ci.zone_id AS zona_id, DATE(nr.created_at) AS dia,
           SUM(-nr.value_paid) AS real_recaudado
    FROM new_recaudo nr
    JOIN credit c            ON c.id  = nr.credit_id
    JOIN credit_intention ci ON ci.id = c.credit_intention_id
    WHERE nr.value_paid < 0
    GROUP BY ci.zone_id, DATE(nr.created_at)
) r ON r.zona_id = dr.zona_id AND r.dia = dr.create_recaudo
WHERE dr.value <> COALESCE(r.real_recaudado, 0)
ORDER BY ABS(dr.value - COALESCE(r.real_recaudado, 0)) DESC;
```

Lectura de resultados:

- `diferencia > 0` → dashboard inflado por sobrantes (causa 2).
- `diferencia < 0` → recaudos de solo capital o solo interés, que crean fila pero no suman al
  dashboard (§4.3).

Como `dashboard_recaudo` y `debido_cobrar` son **acumuladores**, ambos errores llevan tiempo
sumándose sobre las mismas filas y no se pueden separar día por día sin recalcular desde
`new_recaudo`.

---

## 7. Corrección propuesta (no aplicada)

En `NewRecaudoAdapter.java:623`, usar lo aplicado y no llamar si no se aplicó nada:

```java
BigDecimal totalAplicado = requestDto.getValuePaid()
        .subtract(saldoRestante.max(BigDecimal.ZERO));

if (totalAplicado.compareTo(BigDecimal.ZERO) > 0) {
    procesarRecaudo(dataZona.getId(), dataZona.getCode(), totalAplicado);
}
```

El `if` importa tanto como el cálculo: cubre S2 y S3, donde el loop no crea ninguna fila y hoy se suma
el monto completo sin rastro en `new_recaudo`.

Trabajo asociado, en orden:

1. Corregir la línea 623.
2. **Recalcular** las filas ya corrompidas de `dashboard_recaudo` y `debido_cobrar` desde
   `new_recaudo` (el error está persistido y es acumulativo).
3. Decidir qué hacer con `processSingleComponentPayment` y el dashboard (§4.3).
4. Resolver la agregación zona vs. asesor (§3.1) antes de que una zona tenga dos asesores.
5. Filtrar por cierre el `UNION ALL` de cuotas iniciales (§3.3).
6. Persistir el valor solicitado, o al menos el `saldoSobrante`, para que este tipo de caso sea
   auditable desde la BD y no solo desde logs (§5.2).
7. Rechazar o exigir confirmación cuando `valuePaid` excede la deuda pendiente del crédito, en vez de
   aceptar el pago y devolver un sobrante silencioso.
