# Guía de validación — Job de Snapshot de Cartera (`portfolio_snapshot`)

Este documento explica, campo por campo, de dónde sale cada valor que el job diario inserta en
`portfolio_snapshot`, y trae una consulta SQL independiente para volver a calcular ese valor desde
las tablas fuente y compararlo contra lo insertado. El objetivo es poder sustentar cualquier dato
de cartera con evidencia (no "confiar" en el job, sino poder demostrar de dónde sale cada número).

> Todas las consultas son de solo lectura (`SELECT`). Ninguna modifica datos.

## 1. Flujo del job (referencia rápida)

```
SimpleJobScheduler.portfolioSnapshotDailyJob()      cron: app.scheduler.cron.portfolio-snapshot
  └─ PortfolioSnapshotOrchestrator.run(fecha)         fecha = LocalDate.now() en producción
       ├─ limpiarSnapshotPrevio(fecha)                 DELETE portfolio_snapshot WHERE snapshot_date = fecha
       ├─ CreditSnapshotSourceRepository.findActiveCreditsSnapshot(fecha, page)   ← query fuente (todo el cálculo vive aquí)
       └─ procesarPagina(...) → mapToEntity(...) → INSERT en portfolio_snapshot (+ calificación de riesgo)
```

- Query fuente: `src/main/java/com/recaudo/api/infrastructure/repository/CreditSnapshotSourceRepository.java`
- Mapeo a entidad: `src/main/java/com/recaudo/api/domain/gateway/impl/PortfolioSnapshotTransactionalOps.java`
- Disparo manual para pruebas: `POST /dev/other-concepts/run-snapshot?fecha=YYYY-MM-DD`
  (`TestSchedulerController`, ejecuta el job real contra la BD configurada en el perfil activo).

**Conexión BD**: ver `src/main/resources/application-scheduler.yml` (perfil activo del scheduler)
y `src/main/resources/application.yml` (perfil por defecto). Ojo: apuntan a bases distintas
(`recaudo` vs `recaudo_dev`) — confirma con qué perfil corrió el job antes de validar, para no
comparar contra la base equivocada.

## 2. Catálogo de conceptos usado en los cálculos (`glotypes`, key = `TIPCON`)

| concept_id | code | Significado | Tabla donde se guarda el movimiento |
|---|---|---|---|
| 48 | VIV | Valor inversión (capital) | `credit_amortization_detail` (generado) / `new_recaudo_detail` (pagado) |
| 49 | VIT | Valor interés | `credit_amortization_detail` / `new_recaudo_detail` |
| 50 | SV | Seguro de vida | `credit_amortization_detail` / `new_recaudo_detail` |
| 51 | SC | Seguro de cartera | `credit_amortization_detail` / `new_recaudo_detail` |
| 52 | IMT | Interés moratorio (mora causada) | `credit_other_concepts_detail` (positivo) |
| 53 | RECAMORA | Recaudo de mora (mora pagada) | `credit_other_concepts_detail` (se guarda en negativo al pagar, ver §2.1) |
| 54 | REVEMORA | Reverso de mora (al reversar un recaudo) | `credit_other_concepts_detail` (positivo) |

**2.1 — Importante:** la mora (52/53/54) **nunca** vive en `new_recaudo_detail`, solo en
`credit_other_concepts_detail`. Esto se confirmó revisando `NewRecaudoAdapter.java`
(`saveOtherConceptsDetailIfNonZero`, línea ~571, y `reverseRecaudos`, línea ~734) y las queries de
`DailyCollectionRepository.java`. La query fuente del snapshot ya se corrigió para respetar esto
(ver sesión anterior — antes buscaba 53/54 en `new_recaudo_detail` y siempre daba 0 de mora pagada).

Query para reconfirmar el catálogo en cualquier momento (por si cambia el `id` en otro ambiente):

```sql
SELECT id, code, name
FROM glotypes
WHERE `key` = 'TIPCON'
ORDER BY id;
```

## 3. Cómo validar un registro puntual

Define primero los dos parámetros que vas a usar en todas las queries de abajo:

```sql
SET @fecha     = '2026-09-03';   -- fecha de corte a validar (snapshot_date)
SET @credit_id = 1234;           -- crédito puntual a auditar
```

Trae el registro insertado por el job para ese crédito y fecha:

```sql
SELECT *
FROM portfolio_snapshot
WHERE snapshot_date = @fecha
  AND credit_id = @credit_id;
```

Con ese registro a la vista, compara cada campo contra la query correspondiente de la sección 4.

## 4. Origen y validación de cada campo

### 4.1 Identificación y datos del crédito (no calculados, solo joins directos)

`personId, clienteFullname, clienteDocumento, zonaId, zonaNombre, estadoCredito, creditLineId,
creditLineNombre, periodId, periodNombre, periodCodigo, taxTypeId, taxTypeNombre, taxValue,
cuotasPlaneadas`

```sql
SELECT
    c.id                AS creditId,
    c.person_id         AS personId,
    p.fullname          AS clienteFullname,
    p.document          AS clienteDocumento,
    ci.zone_id          AS zonaId,
    z.value             AS zonaNombre,
    c.credit_status     AS estadoCredito,
    cl.id               AS creditLineId,
    cl.name             AS creditLineNombre,
    pe.id               AS periodId,
    pe.name             AS periodNombre,
    pe.cod              AS periodCodigo,
    tt.id               AS taxTypeId,
    tt.name             AS taxTypeNombre,
    c.tax_value         AS taxValue,
    c.period_quantity   AS cuotasPlaneadas
FROM credit c
INNER JOIN person p            ON p.id = c.person_id
INNER JOIN credit_intention ci ON ci.id = c.credit_intention_id
LEFT  JOIN zona z              ON z.id = ci.zone_id
LEFT  JOIN credit_line cl      ON cl.id = c.credit_line_id
LEFT  JOIN period pe           ON pe.id = c.period_id
LEFT  JOIN tax_type tt         ON tt.id = c.tax_type_id
WHERE c.id = @credit_id;
```

⚠️ **`estadoCredito` refleja el estado ACTUAL del crédito en `credit`, no el histórico a la fecha
de corte.** Si el crédito fue cancelado después de la fecha del snapshot, este campo mostrará
`CANCELLED` aunque en `@fecha` estuviera `ACTIVE`. Solo es fiable si validas snapshots del día en
que corrió el job (no para recalcular fechas pasadas).

### 4.2 Cuotas (`totalCuotas`, `cuotasPagadas`, `cuotasPendientes`)

```sql
SELECT
    COUNT(*)                                              AS totalCuotas,
    SUM(CASE WHEN ca.paid_full = 'S' THEN 1 ELSE 0 END)   AS cuotasPagadas,
    SUM(CASE WHEN ca.paid_full = 'S' THEN 0 ELSE 1 END)   AS cuotasPendientes
FROM credit_amortization ca
WHERE ca.credit_id = @credit_id
  AND ca.expiration_date <= @fecha;
```

⚠️ Igual que arriba: `paid_full` es el estado **actual** de la cuota. Si una cuota vencida antes de
`@fecha` se pagó DESPUÉS de esa fecha, aquí ya aparecerá como pagada aunque en la fecha de corte no
lo estuviera. Válido para "hoy", no para recomputar historial.

### 4.3 Capital / Interés / Seg. Vida / Seg. Cartera — Generado

```sql
SELECT
    SUM(CASE WHEN cad.concept_id = 48 THEN cad.value ELSE 0 END) AS capitalGenerado,
    SUM(CASE WHEN cad.concept_id = 49 THEN cad.value ELSE 0 END) AS interesGenerado,
    SUM(CASE WHEN cad.concept_id = 50 THEN cad.value ELSE 0 END) AS seguroVidaGenerado,
    SUM(CASE WHEN cad.concept_id = 51 THEN cad.value ELSE 0 END) AS seguroCarteraGenerado
FROM credit_amortization ca
JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
WHERE ca.credit_id = @credit_id
  AND ca.expiration_date <= @fecha
  AND cad.concept_id IN (48, 49, 50, 51);
```

Esto es el plan de amortización original (no cambia con el tiempo), filtrado a solo las cuotas
cuyo vencimiento ya llegó a la fecha de corte. Es el único bloque que es 100% histórico-seguro,
porque `credit_amortization_detail` no se modifica después de crear el crédito.

### 4.4 Capital / Interés / Seg. Vida / Seg. Cartera — Pagado

```sql
SELECT
    SUM(CASE WHEN nrd.concept_id = 48 THEN ABS(nrd.value) ELSE 0 END) AS capitalPagado,
    SUM(CASE WHEN nrd.concept_id = 49 THEN ABS(nrd.value) ELSE 0 END) AS interesPagado,
    SUM(CASE WHEN nrd.concept_id = 50 THEN ABS(nrd.value) ELSE 0 END) AS seguroVidaPagado,
    SUM(CASE WHEN nrd.concept_id = 51 THEN ABS(nrd.value) ELSE 0 END) AS seguroCarteraPagado
FROM new_recaudo nr
JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
WHERE nr.credit_id = @credit_id
  AND nr.value_paid < 0
  AND nr.created_at < (@fecha + INTERVAL 1 DAY)
  AND nrd.concept_id IN (48, 49, 50, 51);
```

`nr.value_paid < 0` filtra solo recaudos (no reversiones — las reversiones se guardan con
`value_paid` positivo, ver `reverseRecaudos` en `NewRecaudoAdapter`). El corte
`created_at < fecha + 1 día` es correcto porque incluye todo el día de la fecha de corte.

`*Pendiente` = `*Generado − *Pagado` (resta simple, no tiene query propia, valida con los dos
resultados de arriba).

**Para ver el detalle transacción por transacción** (útil si el total no cuadra):

```sql
SELECT nr.id AS recaudoId, nr.created_at, nr.value_paid, nrd.concept_id, nrd.value
FROM new_recaudo nr
JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
WHERE nr.credit_id = @credit_id
  AND nrd.concept_id IN (48, 49, 50, 51)
ORDER BY nr.created_at;
```

### 4.5 Mora generada (`moraGenerada`)

```sql
SELECT SUM(cocd.value) AS moraGenerada
FROM credit_other_concepts coc
JOIN credit_other_concepts_detail cocd ON cocd.credit_other_concepts_id = coc.id
WHERE coc.credit_id = @credit_id
  AND cocd.concept_id = 52          -- IMT
  AND cocd.created_at < (@fecha + INTERVAL 1 DAY);
```

Cada fila de IMT es el interés moratorio calculado por `MoraConceptCalculator` (un registro por
corrida diaria del job de mora — ver `credit_other_concepts_detail` para el detalle día a día).

### 4.6 Mora pagada (`moraPagada`)

```sql
SELECT
    SUM(CASE WHEN cocd.concept_id = 53 THEN ABS(cocd.value) ELSE 0 END) AS moraPagada,
    SUM(CASE WHEN cocd.concept_id = 54 THEN ABS(cocd.value) ELSE 0 END) AS moraReversada,
    SUM(CASE WHEN cocd.concept_id = 53 THEN ABS(cocd.value) ELSE 0 END)
      - SUM(CASE WHEN cocd.concept_id = 54 THEN ABS(cocd.value) ELSE 0 END) AS moraPagadaNeta
FROM credit_other_concepts coc
JOIN credit_other_concepts_detail cocd ON cocd.credit_other_concepts_id = coc.id
WHERE coc.credit_id = @credit_id
  AND cocd.concept_id IN (53, 54)   -- RECAMORA, REVEMORA
  AND cocd.created_at < (@fecha + INTERVAL 1 DAY);
```

`moraPagada` en `portfolio_snapshot` = `moraPagadaNeta` de esta query (pagado − reversado).
`moraPendiente` = `moraGenerada − moraPagadaNeta`.

Si quieres ver la traza completa de mora (causada, pagada, reversada) en orden cronológico para el
crédito:

```sql
SELECT cocd.created_at, g.code, cocd.value, coc.quota_number
FROM credit_other_concepts coc
JOIN credit_other_concepts_detail cocd ON cocd.credit_other_concepts_id = coc.id
JOIN glotypes g ON g.id = cocd.concept_id
WHERE coc.credit_id = @credit_id
  AND g.code IN ('IMT', 'RECAMORA', 'REVEMORA')
ORDER BY cocd.created_at;
```

### 4.7 Días de mora (`diasMora`)

```sql
SELECT DATEDIFF(@fecha, MIN(ca.expiration_date)) AS diasMora
FROM credit_amortization ca
WHERE ca.credit_id = @credit_id
  AND ca.paid_full <> 'S'
  AND ca.expiration_date < @fecha;
```

Si no hay filas (crédito al día), `diasMora` debe ser `0` en el snapshot (el `COALESCE(...,0)` de
la query fuente lo cubre).

⚠️ Mismo caveat de `paid_full` actual vs histórico que en 4.2: si la cuota más antigua vencida ya
se pagó después de `@fecha`, esta cuenta no la va a considerar "vencida" aunque en la fecha de
corte sí lo estuviera.

### 4.8 Calificación de riesgo (`ratingValue`, `ratingRangeStart`, `ratingRangeEnd`)

No está en la query nativa — se calcula en Java después, con el `diasMora` ya resuelto
(`PortfolioSnapshotTransactionalOps.calcularCalificacion`):

```sql
SELECT *
FROM credit_rating_range
WHERE start <= @dias_mora
  AND (`end` IS NULL OR `end` >= @dias_mora);
```

(reemplaza `@dias_mora` por el valor validado en 4.7). Si no hay ningún rango que matchee,
el snapshot debe traer `ratingValue = 'N/A'` y los rangos en `NULL`.

### 4.9 Totales (`totalPagado`, `saldoTotal`)

No tienen tabla propia — son sumas de los bloques anteriores:

- `totalPagado` = `capitalPagado + interesPagado + seguroVidaPagado + seguroCarteraPagado + moraPagadaNeta`
- `saldoTotal` = `capitalPendiente + interesPendiente + seguroVidaPendiente + seguroCarteraPendiente + moraPendiente`

Si estos no cuadran pero cada componente individual sí, el error está en la query nativa (revisar
`CreditSnapshotSourceRepository.findActiveCreditsSnapshot`), no en los datos fuente.

### 4.10 `otrosConceptosGenerado`

En el código actual está *hardcodeado* a `0` (`ROUND(0, 2) AS otrosConceptosGenerado` en la query
nativa). No hay nada que validar contra BD — si en algún momento se necesita que refleje datos
reales, es un cambio de alcance pendiente, no un bug de datos.

## 5. Query de reconciliación total (un solo crédito, un solo pegue)

Corre esto y compara **fila contra fila** contra el `SELECT * FROM portfolio_snapshot WHERE
credit_id = @credit_id AND snapshot_date = @fecha` de la sección 3:

```sql
WITH
cuotas AS (
    SELECT ca.credit_id,
           COUNT(*) AS total_cuotas,
           SUM(CASE WHEN ca.paid_full = 'S' THEN 1 ELSE 0 END) AS cuotas_pagadas
    FROM credit_amortization ca
    WHERE ca.credit_id = @credit_id
      AND ca.expiration_date <= @fecha
    GROUP BY ca.credit_id
),
generado AS (
    SELECT ca.credit_id,
           SUM(CASE WHEN cad.concept_id = 48 THEN cad.value ELSE 0 END) AS capital,
           SUM(CASE WHEN cad.concept_id = 49 THEN cad.value ELSE 0 END) AS interes,
           SUM(CASE WHEN cad.concept_id = 50 THEN cad.value ELSE 0 END) AS seg_vida,
           SUM(CASE WHEN cad.concept_id = 51 THEN cad.value ELSE 0 END) AS seg_cartera
    FROM credit_amortization ca
    JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
    WHERE ca.credit_id = @credit_id
      AND ca.expiration_date <= @fecha
      AND cad.concept_id IN (48, 49, 50, 51)
    GROUP BY ca.credit_id
),
pagado AS (
    SELECT nr.credit_id,
           SUM(CASE WHEN nrd.concept_id = 48 THEN ABS(nrd.value) ELSE 0 END) AS capital,
           SUM(CASE WHEN nrd.concept_id = 49 THEN ABS(nrd.value) ELSE 0 END) AS interes,
           SUM(CASE WHEN nrd.concept_id = 50 THEN ABS(nrd.value) ELSE 0 END) AS seg_vida,
           SUM(CASE WHEN nrd.concept_id = 51 THEN ABS(nrd.value) ELSE 0 END) AS seg_cartera
    FROM new_recaudo nr
    JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
    WHERE nr.credit_id = @credit_id
      AND nr.value_paid < 0
      AND nr.created_at < (@fecha + INTERVAL 1 DAY)
      AND nrd.concept_id IN (48, 49, 50, 51)
    GROUP BY nr.credit_id
),
mora_gen AS (
    SELECT coc.credit_id, SUM(cocd.value) AS generado
    FROM credit_other_concepts coc
    JOIN credit_other_concepts_detail cocd ON cocd.credit_other_concepts_id = coc.id
    WHERE coc.credit_id = @credit_id
      AND cocd.concept_id = 52
      AND cocd.created_at < (@fecha + INTERVAL 1 DAY)
    GROUP BY coc.credit_id
),
mora_pagado AS (
    SELECT coc.credit_id,
           SUM(CASE WHEN cocd.concept_id = 53 THEN ABS(cocd.value) ELSE 0 END) AS pagado,
           SUM(CASE WHEN cocd.concept_id = 54 THEN ABS(cocd.value) ELSE 0 END) AS reversado
    FROM credit_other_concepts coc
    JOIN credit_other_concepts_detail cocd ON cocd.credit_other_concepts_id = coc.id
    WHERE coc.credit_id = @credit_id
      AND cocd.concept_id IN (53, 54)
      AND cocd.created_at < (@fecha + INTERVAL 1 DAY)
    GROUP BY coc.credit_id
),
mora_dias AS (
    SELECT ca.credit_id, MIN(ca.expiration_date) AS fecha_vencimiento_mas_antigua
    FROM credit_amortization ca
    WHERE ca.credit_id = @credit_id
      AND ca.paid_full <> 'S'
      AND ca.expiration_date < @fecha
    GROUP BY ca.credit_id
)
SELECT
    @credit_id                                                              AS creditId,
    cuotas.total_cuotas                                                     AS totalCuotas,
    cuotas.cuotas_pagadas                                                   AS cuotasPagadas,
    (cuotas.total_cuotas - cuotas.cuotas_pagadas)                           AS cuotasPendientes,
    ROUND(COALESCE(generado.capital, 0), 2)                                 AS capitalGenerado,
    ROUND(COALESCE(pagado.capital, 0), 2)                                   AS capitalPagado,
    ROUND(COALESCE(generado.capital, 0) - COALESCE(pagado.capital, 0), 2)   AS capitalPendiente,
    ROUND(COALESCE(generado.interes, 0), 2)                                 AS interesGenerado,
    ROUND(COALESCE(pagado.interes, 0), 2)                                   AS interesPagado,
    ROUND(COALESCE(generado.interes, 0) - COALESCE(pagado.interes, 0), 2)   AS interesPendiente,
    ROUND(COALESCE(generado.seg_vida, 0), 2)                                AS seguroVidaGenerado,
    ROUND(COALESCE(pagado.seg_vida, 0), 2)                                  AS seguroVidaPagado,
    ROUND(COALESCE(generado.seg_vida, 0) - COALESCE(pagado.seg_vida, 0), 2) AS seguroVidaPendiente,
    ROUND(COALESCE(generado.seg_cartera, 0), 2)                             AS seguroCarteraGenerado,
    ROUND(COALESCE(pagado.seg_cartera, 0), 2)                               AS seguroCarteraPagado,
    ROUND(COALESCE(generado.seg_cartera, 0) - COALESCE(pagado.seg_cartera, 0), 2) AS seguroCarteraPendiente,
    ROUND(COALESCE(mora_gen.generado, 0), 2)                                AS moraGenerada,
    ROUND(COALESCE(mora_pagado.pagado, 0) - COALESCE(mora_pagado.reversado, 0), 2) AS moraPagada,
    ROUND(COALESCE(mora_gen.generado, 0)
        - (COALESCE(mora_pagado.pagado, 0) - COALESCE(mora_pagado.reversado, 0)), 2) AS moraPendiente,
    ROUND(
        COALESCE(pagado.capital, 0) + COALESCE(pagado.interes, 0)
      + COALESCE(pagado.seg_vida, 0) + COALESCE(pagado.seg_cartera, 0)
      + (COALESCE(mora_pagado.pagado, 0) - COALESCE(mora_pagado.reversado, 0))
    , 2) AS totalPagado,
    ROUND(
        (COALESCE(generado.capital, 0)     - COALESCE(pagado.capital, 0))
      + (COALESCE(generado.interes, 0)     - COALESCE(pagado.interes, 0))
      + (COALESCE(generado.seg_vida, 0)    - COALESCE(pagado.seg_vida, 0))
      + (COALESCE(generado.seg_cartera, 0) - COALESCE(pagado.seg_cartera, 0))
      + (COALESCE(mora_gen.generado, 0)
            - (COALESCE(mora_pagado.pagado, 0) - COALESCE(mora_pagado.reversado, 0)))
    , 2) AS saldoTotal,
    COALESCE(DATEDIFF(@fecha, mora_dias.fecha_vencimiento_mas_antigua), 0)  AS diasMora
FROM cuotas
LEFT JOIN generado    ON generado.credit_id    = cuotas.credit_id
LEFT JOIN pagado      ON pagado.credit_id      = cuotas.credit_id
LEFT JOIN mora_gen    ON mora_gen.credit_id    = cuotas.credit_id
LEFT JOIN mora_pagado ON mora_pagado.credit_id = cuotas.credit_id
LEFT JOIN mora_dias   ON mora_dias.credit_id   = cuotas.credit_id;
```

Si esta consulta da exactamente lo mismo que la fila de `portfolio_snapshot`, el registro está
sustentado end-to-end. Si difiere en algo, ya sabes en qué CTE de la sección 4 mirar el detalle.

## 6. Validaciones a nivel de lote / global (para cuadrar totales, no un crédito puntual)

**6.1 — El total de créditos procesados por el job debe cuadrar con los créditos activos actuales:**

```sql
SELECT COUNT(*) FROM credit c WHERE c.credit_status = 'ACTIVE';
```

Debe ser igual a `totalCreditosProcesados` que devuelve el job (el resumen que loguea
`SimpleJobScheduler` / lo que retorna `POST /dev/other-concepts/run-snapshot`). Ojo: **sin**
filtro de `created_at` — la query fuente no lo tiene (ver sesión previa, error común al validar).

**6.2 — El número de filas insertadas en `portfolio_snapshot` para la fecha debe coincidir:**

```sql
SELECT COUNT(*) FROM portfolio_snapshot WHERE snapshot_date = @fecha;
```

Debe ser igual a 6.1 (uno a uno, ya que el `credit_id` es único por `snapshot_date`, ver
`uq_snapshot_credit_date` en `PortfolioSnapshotEntity`).

**6.3 — Ningún crédito activo debe faltar en el snapshot del día:**

```sql
SELECT c.id
FROM credit c
WHERE c.credit_status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM portfolio_snapshot s
      WHERE s.credit_id = c.id AND s.snapshot_date = @fecha
  );
```

Debe devolver 0 filas. Si devuelve algo, hubo un error de página durante el job (revisar logs de
`[PortfolioSnapshot] Error en página...` — el job aborta todo si una página falla, así que si hay
faltantes es porque el job no terminó o falló antes de esa página).

**6.4 — Totales agregados por zona** (para cuadrar contra un reporte gerencial de una zona):

```sql
SELECT
    s.zona_id,
    MAX(s.zona_nombre)                AS zona,
    COUNT(*)                          AS creditos,
    SUM(s.saldo_total)                AS saldoTotalZona,
    SUM(s.mora_pendiente)             AS moraPendienteZona,
    SUM(s.capital_pendiente)          AS capitalPendienteZona
FROM portfolio_snapshot s
WHERE s.snapshot_date = @fecha
GROUP BY s.zona_id
ORDER BY s.zona_id;
```

## 7. Limitaciones conocidas (no son bugs de dato, son de diseño — repórtalas si necesitas cambiarlas)

1. **`estadoCredito`, `cuotasPagadas`, `diasMora` usan el estado ACTUAL de `credit.credit_status` y
   `credit_amortization.paid_full`, no un histórico versionado.** El job es confiable cuando corre
   para "hoy" (como está el cron), pero si se re-ejecuta manualmente para una fecha pasada
   (`POST /dev/other-concepts/run-snapshot?fecha=2026-08-01`), estos tres campos van a reflejar el
   estado de HOY, no el de esa fecha histórica. Si necesitan recomputar fechas pasadas de forma
   fiable, hay que trackear el historial de `paid_full`/`credit_status` (auditoría por fecha), lo
   cual es un cambio de alcance mayor.
2. **`otrosConceptosGenerado` está hardcodeado a 0** — no hay fuente de datos conectada todavía.
3. La query fuente solo trae créditos con `credit_status = 'ACTIVE'` **al momento de correr el
   job** — un crédito cancelado hoy no vuelve a aparecer en snapshots futuros ni si se reprocesa
   uno pasado, aunque ese día estuviera activo.

## 8. Cómo volver a correr el job para una fecha concreta (pruebas)

```
POST /dev/other-concepts/run-snapshot?fecha=2026-09-03
```

Responde el mismo `PortfolioSnapshotSummary` que ves en logs: `totalCreditosProcesados`,
`paginasConError`, `jobExecutionId`. El `jobExecutionId` queda grabado en cada fila de
`portfolio_snapshot.job_execution_id` — útil para aislar qué corrida generó cada dato si el job se
disparó varias veces el mismo día (recuerda que `limpiarSnapshotPrevio` borra todo lo de esa
`snapshot_date` antes de reinsertar, así que solo queda la última corrida).
