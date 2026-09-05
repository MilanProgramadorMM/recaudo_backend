# Guía de validación — Cartera de un cliente a fecha de corte (`PortfolioSnapshotController`)

Este documento cubre los endpoints de **cliente** de `PortfolioSnapshotController` — no el job diario en sí
(eso ya está en [`cartera-snapshot-validacion.md`](./cartera-snapshot-validacion.md)). Aquí el objetivo es
otro: estos endpoints **no recalculan nada nuevo**, solo agregan (`SUM`/`COUNT`) filas ya existentes en
`portfolio_snapshot` para un `person_id`. Por eso, antes de dudar de un número de un cliente, primero
confirma con la guía anterior que las filas por-crédito de `portfolio_snapshot` ya están bien — si esas
están mal, esta agregación va a heredar el error sin que se note aquí.

> Todas las consultas son de solo lectura (`SELECT`).

## 1. Flujo

```
PortfolioSnapshotController
  ├─ GET /portfolio-snapshots/client/{personId}?date=...              → getClientState
  ├─ GET /portfolio-snapshots/client/{personId}/history?startDate=...&endDate=... → getClientHistory
  └─ GET /portfolio-snapshots/clients?date=...&search=...             → getClients (buscador)
       │
       ▼
PortfolioSnapshotUseCase → PortfolioSnapshotAdapter (implementa PortfolioSnapshotIGateway)
       │
       ▼
PortfolioSnapshotRepository
  ├─ findClientAggregateByDate(fecha, personId)        → 1 fila agregada (todos los créditos del cliente)
  ├─ findClientDailyAggregates(personId, start, end)   → 1 fila agregada por día del rango
  └─ findClientsBySnapshotDate(fecha, busqueda)         → 1 fila agregada por cliente (buscador/listado)
       │
       ▼
tabla portfolio_snapshot   (ya poblada por el job diario — ver la otra guía)
```

**Punto clave:** las tres queries de arriba **agrupan por `person_id`** sobre `portfolio_snapshot`, sumando
los valores de **todos los créditos activos de ese cliente** en la fecha (o rango) pedida. `PortfolioSnapshotAdapter`
(`mapClientState`, `mapPuntoCliente`, `calcularResumenEvolucionCliente`) no vuelve a tocar la BD ni recalcula
nada financiero — solo empaqueta lo que ya trajo la query en el DTO de respuesta, y en el caso del historial,
resta el primer día del rango contra el último para armar el resumen de evolución.

## 2. Endpoint puntual: `GET /portfolio-snapshots/client/{personId}?date=YYYY-MM-DD`

### 2.1 Preparar los parámetros

```sql
SET @fecha     = '2026-09-03';
SET @person_id = 3049;   -- personId, no creditId
```

### 2.2 Origen y validación de cada campo de `ClientPortfolioStateDto`

| Campo del DTO | De dónde sale | Cómo validar |
|---|---|---|
| `zonas` | `GROUP_CONCAT(DISTINCT zona_nombre)` partido por `, ` en Java | ver 2.3 |
| `conteos.total` | `COUNT(*)` de filas del cliente ese día | ver 2.3 |
| `conteos.activos/cancelados/inactivos` | `COUNT(CASE WHEN estado_credito = ...)` | ⚠️ ver limitación en §5 — cancelados/inactivos **siempre dan 0** |
| `conteos.enMora` / `alDia` | `COUNT` según `dias_mora > 0` / `= 0` | ver 2.3 |
| `capital/interes/seguroVida/seguroCartera/mora` (`.generado/.pagado/.pendiente`) | `SUM` de las columnas equivalentes de `portfolio_snapshot` | ver 2.3 |
| `otrosConceptosGenerado` | `SUM(otros_conceptos_generado)` — hoy siempre 0 (ver guía del job, §4.10) | ver 2.3 |
| `totalPagado` / `saldoTotal` | `SUM(total_pagado)` / `SUM(saldo_total)` | ver 2.3 |
| `cuotas.*` | `SUM(cuotas_planeadas/total_cuotas/cuotas_pagadas/cuotas_pendientes)` | ver 2.3 |
| `diasMoraMaximo` | `MAX(dias_mora)` | ver 2.3 |
| `diasMoraPromedio` | `ROUND(AVG(COALESCE(dias_mora,0)), 2)` | ver 2.3 |

### 2.3 Query de reconciliación completa (un solo pegue)

Corre esto y compáralo campo a campo contra la respuesta JSON del endpoint:

```sql
SELECT
    s.person_id                                                       AS personId,
    MAX(s.cliente_fullname)                                           AS clienteFullname,
    MAX(s.cliente_documento)                                          AS clienteDocumento,
    GROUP_CONCAT(DISTINCT s.zona_nombre ORDER BY s.zona_nombre SEPARATOR ', ') AS zonaNombres,
    COUNT(*)                                                          AS totalCreditos,
    COUNT(CASE WHEN s.estado_credito = 'ACTIVE'    THEN 1 END)        AS creditosActivos,
    COUNT(CASE WHEN s.estado_credito = 'CANCELLED' THEN 1 END)        AS creditosCancelados,
    COUNT(CASE WHEN s.estado_credito = 'INACTIVE'  THEN 1 END)        AS creditosInactivos,
    COUNT(CASE WHEN s.dias_mora > 0 THEN 1 END)                       AS creditosEnMora,
    COUNT(CASE WHEN COALESCE(s.dias_mora, 0) = 0 THEN 1 END)          AS creditosAlDia,
    COALESCE(SUM(s.capital_generado), 0)                             AS capitalGenerado,
    COALESCE(SUM(s.capital_pagado), 0)                               AS capitalPagado,
    COALESCE(SUM(s.capital_pendiente), 0)                            AS capitalPendiente,
    COALESCE(SUM(s.interes_generado), 0)                             AS interesGenerado,
    COALESCE(SUM(s.interes_pagado), 0)                               AS interesPagado,
    COALESCE(SUM(s.interes_pendiente), 0)                            AS interesPendiente,
    COALESCE(SUM(s.seguro_vida_generado), 0)                         AS seguroVidaGenerado,
    COALESCE(SUM(s.seguro_vida_pagado), 0)                           AS seguroVidaPagado,
    COALESCE(SUM(s.seguro_vida_pendiente), 0)                        AS seguroVidaPendiente,
    COALESCE(SUM(s.seguro_cartera_generado), 0)                      AS seguroCarteraGenerado,
    COALESCE(SUM(s.seguro_cartera_pagado), 0)                        AS seguroCarteraPagado,
    COALESCE(SUM(s.seguro_cartera_pendiente), 0)                     AS seguroCarteraPendiente,
    COALESCE(SUM(s.mora_generada), 0)                                AS moraGenerada,
    COALESCE(SUM(s.mora_pagada), 0)                                  AS moraPagada,
    COALESCE(SUM(s.mora_pendiente), 0)                               AS moraPendiente,
    COALESCE(SUM(s.otros_conceptos_generado), 0)                     AS otrosConceptosGenerado,
    COALESCE(SUM(s.total_pagado), 0)                                 AS totalPagado,
    COALESCE(SUM(s.saldo_total), 0)                                  AS saldoTotal,
    COALESCE(SUM(s.cuotas_planeadas), 0)                             AS cuotasPlaneadas,
    COALESCE(SUM(s.total_cuotas), 0)                                 AS totalCuotas,
    COALESCE(SUM(s.cuotas_pagadas), 0)                               AS cuotasPagadas,
    COALESCE(SUM(s.cuotas_pendientes), 0)                            AS cuotasPendientes,
    MAX(s.dias_mora)                                                 AS diasMoraMaximo,
    ROUND(AVG(COALESCE(s.dias_mora, 0)), 2)                          AS diasMoraPromedio
FROM portfolio_snapshot s
WHERE s.snapshot_date = @fecha
  AND s.person_id = @person_id
GROUP BY s.person_id;
```

Si el cliente no tiene ningún crédito activo en `portfolio_snapshot` para esa fecha, esta query no devuelve
filas — y el endpoint responde `404` (`ResourceNotFoundException`, ver `PortfolioSnapshotAdapter.getClientState`).
Eso es esperado, no un bug.

### 2.4 Ver el detalle crédito por crédito (para saber qué compone la suma)

Si un total no cuadra con lo que esperabas, este es el primer lugar donde mirar — trae cada crédito del
cliente por separado, tal como está en `portfolio_snapshot` para esa fecha:

```sql
SELECT credit_id, estado_credito, zona_nombre, dias_mora, rating_value,
       capital_pendiente, interes_pendiente, mora_pendiente, saldo_total, total_pagado
FROM portfolio_snapshot
WHERE snapshot_date = @fecha
  AND person_id = @person_id
ORDER BY credit_id;
```

Suma manualmente `saldo_total` de esta lista y compárala contra el `saldoTotal` del paso 2.3 — deben coincidir
exacto. Si no coinciden, el problema no está en la agregación del cliente sino en cómo se está filtrando
`person_id` (verifica que no haya dos `person_id` distintos para el mismo cliente por duplicidad de datos en
`credit`/`person`).

## 3. Endpoint de historial: `GET /portfolio-snapshots/client/{personId}/history?startDate=...&endDate=...`

### 3.1 La serie diaria

Cada elemento de `serie[]` en la respuesta es exactamente una fila de esta query (una por día donde el
cliente tuvo al menos un crédito activo):

```sql
SET @person_id  = 3049;
SET @start_date = '2026-08-01';
SET @end_date   = '2026-09-03';

SELECT
    s.snapshot_date                                                    AS snapshotDate,
    COUNT(*)                                                          AS totalCreditos,
    COUNT(CASE WHEN s.estado_credito = 'ACTIVE'    THEN 1 END)        AS creditosActivos,
    COUNT(CASE WHEN s.estado_credito = 'CANCELLED' THEN 1 END)        AS creditosCancelados,
    COUNT(CASE WHEN s.dias_mora > 0 THEN 1 END)                       AS creditosEnMora,
    COALESCE(SUM(s.saldo_total), 0)                                  AS saldoTotal,
    COALESCE(SUM(s.capital_pendiente), 0)                            AS capitalPendiente,
    COALESCE(SUM(s.interes_pendiente), 0)                            AS interesPendiente,
    COALESCE(SUM(s.mora_pendiente), 0)                               AS moraPendiente,
    COALESCE(SUM(s.capital_pagado), 0)                               AS capitalPagado,
    COALESCE(SUM(s.total_pagado), 0)                                 AS totalPagado,
    COALESCE(SUM(s.cuotas_pagadas), 0)                               AS cuotasPagadas,
    COALESCE(SUM(s.cuotas_pendientes), 0)                            AS cuotasPendientes,
    MAX(s.dias_mora)                                                 AS diasMoraMaximo,
    ROUND(AVG(COALESCE(s.dias_mora, 0)), 2)                          AS diasMoraPromedio
FROM portfolio_snapshot s
WHERE s.person_id = @person_id
  AND s.snapshot_date BETWEEN @start_date AND @end_date
GROUP BY s.snapshot_date
ORDER BY s.snapshot_date;
```

`diasConSnapshot` en la respuesta debe ser igual a `COUNT(*)` de filas que devuelva esta query (una fila =
un día con snapshot).

### 3.2 El resumen de evolución (`resumenEvolucion`)

No es una query nueva — es aritmética simple en Java (`calcularResumenEvolucionCliente`) sobre el **primer**
y el **último** día de la serie del paso 3.1. Para validarlo, toma la primera y la última fila de esa serie y
verifica:

```
variacionSaldo            = saldoTotal(último día) − saldoTotal(primer día)
pctVariacionSaldo          = variacionSaldo × 100 / saldoTotal(primer día)     -- null si el primer día es 0
variacionCapitalPendiente  = capitalPendiente(último) − capitalPendiente(primero)
variacionMoraPendiente     = moraPendiente(último) − moraPendiente(primero)
totalRecuperado            = totalPagado(último) − totalPagado(primero)
capitalRecuperado          = capitalPagado(último) − capitalPagado(primero)
variacionCreditosEnMora    = creditosEnMora(último) − creditosEnMora(primero)
```

⚠️ Ojo con la interpretación: `totalRecuperado`/`capitalRecuperado` NO son "lo que pagó el cliente en el
rango" en un sentido de flujo de caja diario — son la diferencia entre el acumulado histórico de pagos que
trae `total_pagado`/`capital_pagado` en el último snapshot vs. el primero. Si querés el pago real ocurrido
día a día dentro del rango, hay que ir a `new_recaudo`/`credit_other_concepts_detail` directamente (igual que
en la guía del job, sección 4.4/4.6), no a esta serie.

## 4. Buscador de clientes: `GET /portfolio-snapshots/clients?date=...&search=...`

```sql
SET @fecha = '2026-09-03';
SET @busqueda = NULL;   -- o un texto, ej: 'GARCIA' — hace LIKE '%texto%' contra fullname y documento

SELECT
    s.person_id                                              AS personId,
    MAX(s.cliente_fullname)                                  AS clienteFullname,
    MAX(s.cliente_documento)                                 AS clienteDocumento,
    COUNT(*)                                                 AS totalCreditos,
    COALESCE(SUM(s.saldo_total), 0)                          AS saldoTotal,
    MAX(s.dias_mora)                                         AS diasMoraMaximo
FROM portfolio_snapshot s
WHERE s.snapshot_date = @fecha
  AND (@busqueda IS NULL
       OR s.cliente_fullname LIKE CONCAT('%', @busqueda, '%')
       OR s.cliente_documento LIKE CONCAT('%', @busqueda, '%'))
GROUP BY s.person_id
ORDER BY MAX(s.cliente_fullname) ASC;
```

`saldoTotal` y `totalCreditos` de cada fila de este listado deben coincidir con lo que te da la sección 2.3
para ese mismo `personId` — si no, algo se desincronizó (ejecuta ambas con el mismo `@fecha` y compara).

## 5. Limitaciones conocidas (heredadas del job, no bugs nuevos de esta capa)

1. **`conteos.cancelados` y `conteos.inactivos` van a dar siempre 0.** La query los calcula
   (`COUNT(CASE WHEN estado_credito = 'CANCELLED'...)`), pero el job que llena `portfolio_snapshot`
   (`CreditSnapshotSourceRepository.findActiveCreditsSnapshot`) **solo inserta créditos con
   `credit_status = 'ACTIVE'`** — nunca va a existir una fila con `estado_credito = 'CANCELLED'` en la
   tabla. Si el negocio necesita ver "el cliente tuvo 3 créditos, 1 se canceló", esa cuenta no vive aquí (hay
   que armarla contra la tabla `credit` directamente, no contra `portfolio_snapshot`).
2. **Si un crédito del cliente se cancela, desaparece silenciosamente de `totalCreditos` desde ese día en
   adelante** (no aparece como "cancelado", simplemente dej a de sumar). En el historial (sección 3) esto se
   ve como una caída abrupta de `totalCreditos`/`saldoTotal` de un día para otro sin ningún crédito
   "salido" explícito — es el comportamiento esperado dado el punto 1, no una pérdida de datos.
3. **Mismo caveat de `estado_credito`/`dias_mora` "actual vs. histórico" que la guía del job** (sección 7 de
   `cartera-snapshot-validacion.md`): si se reprocesa una fecha pasada manualmente, estos campos reflejan el
   estado de HOY de `credit`/`credit_amortization`, no el de esa fecha histórica.
4. **`GROUP_CONCAT(DISTINCT zona_nombre ...)`** tiene el límite por defecto de MySQL
   (`group_concat_max_len`, normalmente 1024 caracteres) — irrelevante en la práctica salvo que un cliente
   tenga créditos en decenas de zonas distintas con nombres muy largos.

## 6. Cómo probar manualmente

```
GET /portfolio-snapshots/client/3049?date=2026-09-03
GET /portfolio-snapshots/client/3049/history?startDate=2026-08-01&endDate=2026-09-03
GET /portfolio-snapshots/clients?date=2026-09-03&search=MEJIA
```

Todos requieren que el job de snapshot ya se haya corrido para esas fechas (`portfolio_snapshot` con filas
para ese `snapshot_date`) — si no, revisa primero la sección 6 de la guía del job para confirmar que el
snapshot de esa fecha existe antes de reportar esto como un bug del endpoint de cliente.
