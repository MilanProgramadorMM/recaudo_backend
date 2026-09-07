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

0. **⚠️ Los snapshots con `snapshot_date` anterior al 2026-09-01 NO son confiables — no respetan
   fecha de corte en absoluto.** La versión original de la query (commit `2360df0`, "se implementa
   logica de cartera", 2026-07-27) calculaba `generado` sumando el plan de amortización **completo**
   del crédito (pasado y futuro) y `pagado` sumando **todos** los recaudos históricos del crédito,
   sin ningún `WHERE` que filtrara por `:fecha` — el parámetro de fecha prácticamente no se usaba
   para el cálculo financiero, solo para `diasMora` y para etiquetar la fila. El filtro real
   (`WHERE ca.expiration_date <= :fecha`, `nr.created_at < fecha + 1 día`) se agregó recién en el
   commit `233bbd0` ("ajustes basados en el 7 de agosto", aplicado 2026-09-01).
   **Consecuencia:** cualquier fila con `snapshot_date < '2026-09-01'` fue calculada con la lógica
   vieja — su `capitalGenerado`/`interesGenerado`/`seguro*Generado` reflejan el valor **total** del
   crédito (`credit.total_capital_value`, etc.) sin importar si ya había vencido, y su `*Pagado`
   suma pagos que pudieron haber ocurrido después de esa fecha. Esto infla `saldoTotal` y distorsiona
   por completo cualquier comparación de evolución (`getZoneHistory`/`getClientHistory`,
   `resumenEvolucion`) que cruce esa fecha — no es un dato "un poco impreciso", es una magnitud
   completamente distinta a lo que debería ser un snapshot puntual. Verificado con el crédito 691
   de zona ELITE: el 2026-08-06 registró `capitalGenerado ≈ $6.817.524` (≈ `total_capital_value`
   completo, $6.750.000), cuando la primera cuota del crédito no vencía hasta el 2026-08-18 — bajo
   la query corregida, ese valor debería haber sido $0.
   **Recomendación:** no usar snapshots anteriores al 2026-09-01 para análisis histórico o de
   evolución. Ver la sección 9 para el plan de regeneración de esas fechas.

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

## 9. Plan para regenerar los snapshots anteriores al 2026-09-01

Dado el hallazgo de la sección 7.0, los snapshots viejos no se pueden "arreglar" editando filas —
hay que volver a correr el job para cada `snapshot_date` afectada, para que se recalculen con la
query actual (la que sí respeta `expiration_date <= :fecha`).

**Antes de tocar nada**, corre esto para confirmar el rango real afectado (no asumas que es todo
desde el 27 de julio — puede que el job solo se haya usado para pruebas puntuales, no a diario):

```sql
SELECT MIN(snapshot_date) AS primera, MAX(snapshot_date) AS ultima,
       COUNT(DISTINCT snapshot_date) AS dias_distintos, COUNT(*) AS filas_totales
FROM portfolio_snapshot
WHERE snapshot_date < '2026-09-01';
```

Y para ver exactamente qué fechas existen (por si hay huecos y no vale la pena regenerar todo el
rango calendario, solo las fechas que ya tienen datos):

```sql
SELECT snapshot_date, COUNT(*) AS creditos, MAX(job_execution_id) AS ultimo_job
FROM portfolio_snapshot
WHERE snapshot_date < '2026-09-01'
GROUP BY snapshot_date
ORDER BY snapshot_date;
```

**Regenerar** significa llamar, una por una y en orden, `POST /dev/other-concepts/run-snapshot?fecha=X`
para cada `snapshot_date` afectada. Cada llamada hace `DELETE ... WHERE snapshot_date = fecha` y
vuelve a insertar con la query actual — no hay que borrar nada a mano antes.

El endpoint vive bajo `/dev/**`, que **no** está en la lista `permitAll()` de `SecurityConfig`
(está comentado: `//.requestMatchers("/dev/**").permitAll()`), así que necesitas un JWT válido
(`Authorization: Bearer <token>`) para llamarlo. `application.yml` tiene
`jwt-expiration-milliseconds: 60000`, pero en la práctica el token emitido dura **24 horas**
(verificado decodificando un token real: `exp - iat = 86400s`) — de sobra para una corrida de 46+
llamadas, ese valor de config no corresponde 1:1 a segundos de vida del token.

⚠️ **Header obligatorio que falta en cualquier request si no lo agregas:** `VersionFilter`
(`src/main/java/com/recaudo/api/config/VersionFilter.java`) intercepta **todas** las rutas —
incluyendo `/dev/**` y `/auth/login` — y devuelve `426 Upgrade Required` con
`{"message":"Debe actualizar la aplicacion"}` si no viene el header `X-App-Version` con el valor
exacto de `app.version` en `application.yml` (hoy `1.0.3`). Sin este header, **nada** de lo de abajo
funciona, ni siquiera el login.

### 9.1 Paso a paso — levantar el backend contra la BD correcta

```powershell
cd C:\Users\milanmejia\Documents\GitHub\recaudo_backend
$env:JAVA_HOME = "C:\Users\milanmejia\.jdks\temurin-17.0.18"
.\gradlew.bat bootRun
```

**Sin perfil activo** (`bootRun` a secas) usa `application.yml` por defecto, que apunta a
`159.203.163.185/recaudo_dev` — la BD correcta para este procedimiento. **No actives el perfil
`scheduler`** para esto: `application-scheduler.yml` apunta a una BD distinta (`recaudo`, sin
`_dev`) — mismo host, otra base. Espera a ver `Started MainApplication` en el log (20-40s) antes de
seguir.

Antes de levantar una instancia nueva, revisa si ya hay una corriendo en el puerto 8080
(`netstat -ano | grep :8080` en Git Bash, o `Get-NetTCPConnection -LocalPort 8080` en PowerShell) —
si el IDE ya tiene el backend arriba, úsala directamente en vez de duplicar.

### 9.2 Vía PowerShell (`Invoke-RestMethod`)

Conseguir el token:

```powershell
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" `
  -ContentType "application/json" -Headers @{ "X-App-Version" = "1.0.3" } `
  -Body '{"username":"TU_USUARIO","password":"TU_CLAVE"}'
$token = $login.data.token   # ajusta el path si tu respuesta trae el token en otro campo
```

Prueba con una sola fecha primero (ver sección 9.4 para cómo validar el resultado), y si sale bien,
corre el resto en lote:

```powershell
$fechas = @(
  "2026-07-12","2026-07-13","2026-07-14","2026-07-15","2026-07-16","2026-07-17","2026-07-18",
  "2026-07-19","2026-07-20","2026-07-21","2026-07-22","2026-07-23","2026-07-27","2026-07-28",
  "2026-07-29","2026-07-30","2026-07-31","2026-08-01","2026-08-02","2026-08-03","2026-08-04",
  "2026-08-05","2026-08-07","2026-08-08","2026-08-09","2026-08-10","2026-08-11","2026-08-12",
  "2026-08-13","2026-08-14","2026-08-15","2026-08-16","2026-08-17","2026-08-18","2026-08-19",
  "2026-08-20","2026-08-21","2026-08-22","2026-08-23","2026-08-24","2026-08-25","2026-08-26",
  "2026-08-27","2026-08-28","2026-08-29","2026-08-30"
)

foreach ($f in $fechas) {
  try {
    $r = Invoke-RestMethod -Method Post `
      -Uri "http://localhost:8080/api/dev/other-concepts/run-snapshot?fecha=$f" `
      -Headers @{ Authorization = "Bearer $token"; "X-App-Version" = "1.0.3" }
    Write-Host "$f -> OK, procesados=$($r.totalCreditosProcesados)"
  } catch {
    Write-Host "$f -> ERROR: $($_.Exception.Message)"
    # Si es 401 (token vencido), vuelve a correr el login de arriba y relanza el foreach
    # solo con las fechas que faltan (quítalas de $fechas las que ya salieron OK).
  }
}
```

### 9.3 Vía Postman (Collection Runner)

Postman no tiene un "loop" nativo en una sola request — la forma correcta de iterar 46 fechas es el
**Collection Runner con un archivo de datos (CSV)**:

1. **Crea una Collection** nueva, ej. "Recaudo — Regenerar Snapshots".
2. **Variable de colección** `baseUrl` = `http://localhost:8080/api`.
3. **Header a nivel de Collection** (pestaña **Headers** de la Collection, no de cada request, para
   que aplique a todas): `X-App-Version: 1.0.3` — si no, todo responde `426 Upgrade Required` (ver
   nota arriba de la sección 9 sobre `VersionFilter`).
4. **Request de login** dentro de la colección: `POST {{baseUrl}}/auth/login`, body raw JSON
   `{"username": "...", "password": "..."}`. En la pestaña **Tests** de esa request, agrega:
   ```javascript
   const data = pm.response.json();
   pm.collectionVariables.set("token", data.data.token); // ajusta el path según tu respuesta real
   ```
   Esto guarda el token automáticamente cada vez que corres el login.
5. **Request de regeneración**: `POST {{baseUrl}}/dev/other-concepts/run-snapshot?fecha={{fecha}}`.
   En la pestaña **Authorization**, tipo `Bearer Token`, valor `{{token}}`.
6. **Corre el login manualmente una vez** (botón Send) para tener un token fresco en la variable.
7. **Crea el archivo `fechas.csv`** (en tu equipo, cualquier carpeta) con una columna `fecha`:
   ```
   fecha
   2026-07-12
   2026-07-13
   2026-07-14
   2026-07-15
   2026-07-16
   2026-07-17
   2026-07-18
   2026-07-19
   2026-07-20
   2026-07-21
   2026-07-22
   2026-07-23
   2026-07-27
   2026-07-28
   2026-07-29
   2026-07-30
   2026-07-31
   2026-08-01
   2026-08-02
   2026-08-03
   2026-08-04
   2026-08-05
   2026-08-07
   2026-08-08
   2026-08-09
   2026-08-10
   2026-08-11
   2026-08-12
   2026-08-13
   2026-08-14
   2026-08-15
   2026-08-16
   2026-08-17
   2026-08-18
   2026-08-19
   2026-08-20
   2026-08-21
   2026-08-22
   2026-08-23
   2026-08-24
   2026-08-25
   2026-08-26
   2026-08-27
   2026-08-28
   2026-08-29
   2026-08-30
   ```
8. Abre el **Runner** (botón "Run" sobre la colección, o el ícono del Runner en la barra lateral).
9. Selecciona **solo** la request de regeneración (no incluyas la de login en el run).
10. En **Data File**, sube `fechas.csv` — Postman detecta la columna `fecha` y la cantidad de
    iteraciones automáticamente (46).
11. Pon **Delay** en `0ms` (o `100ms` si quieres ir más despacio) y dale **Run**.
12. Postman ejecuta la request 46 veces, sustituyendo `{{fecha}}` por cada fila del CSV. Al terminar
    ves un resumen pass/fail por iteración, y puedes hacer clic en cualquiera para ver el response body
    completo (`totalCreditosProcesados`, etc.).
13. **Si el token vence a mitad de camino** (verás varias iteraciones con `401`): vuelve a correr el
    request de login (paso 6), edita `fechas.csv` para dejar solo las fechas que fallaron, y vuelve a
    correr el Runner con ese CSV recortado.

### 9.4 Verificar que la regeneración funcionó

```sql
SELECT capital_generado, capital_pagado, capital_pendiente
FROM portfolio_snapshot
WHERE credit_id = 691 AND snapshot_date = '2026-08-06';
```

Debe dar `capital_generado = 0` (la primera cuota del crédito 691 vence el 18-ago, después del
corte). Y a nivel de zona:

```sql
SELECT snapshot_date, COUNT(*) AS creditos, SUM(saldo_total) AS saldoTotal
FROM portfolio_snapshot
WHERE zona_nombre = 'ELITE' AND snapshot_date IN ('2026-08-06', '2026-09-03')
GROUP BY snapshot_date;
```

El `saldoTotal` del 06-ago ya no debería ser una cifra desproporcionada — debería quedar en un rango
comparable al del 03-sep, con la diferencia explicada por pagos reales de ese mes, no por un error
de cálculo.

**Lo que sí va a mejorar** con la regeneración: `capitalGenerado/Pagado/Pendiente`,
`interesGenerado/Pagado/Pendiente`, `seguro*Generado/Pagado/Pendiente`, `moraGenerada/Pagada/Pendiente`,
`totalPagado`, `saldoTotal` — todos van a respetar la fecha de corte real por primera vez.

**Lo que sigue sin ser 100% histórico** incluso después de regenerar (limitación de la sección 7,
punto 1, que es distinta a esta): `cuotasPagadas`, `diasMora` y `estadoCredito` van a reflejar el
estado **actual** de `credit_amortization.paid_full`/`credit.credit_status`, no el que tenían en esa
fecha pasada. Para créditos donde nada cambió de estado desde entonces esto no importa; para
créditos que se pagaron o cancelaron después, esos tres campos del snapshot regenerado seguirán sin
ser exactos — pero ya es una mejora enorme sobre el estado actual (que ni siquiera el saldo estaba bien).

**Antes de ejecutar el regenerado en lote**, confirma explícitamente con el equipo — son escrituras
reales sobre `portfolio_snapshot` (aunque acotadas y reversibles solo re-corriendo de nuevo, no es
una operación trivial de deshacer si se ejecuta contra la base de producción).

## 10. Bitácora — regeneración ejecutada (2026-09-05)

Registro de la corrida real que corrigió los 47 snapshots afectados (sección 7.0). Queda acá para
que quede trazable qué se hizo, contra qué ambiente, y qué se verificó — no repetir esta corrida a
menos que se detecte otro problema en el mismo rango de fechas.

**Ambiente:** backend local (`./gradlew bootRun`, sin perfil activo → `application.yml` por
defecto), apuntando a `159.203.163.185/recaudo_dev`. Usuario `administrador` (rol `Administrador`).
Header `X-App-Version: 1.0.3` en cada request (obligatorio, ver nota en la sección 9 —
esto no estaba documentado antes de esta corrida; se descubrió en el momento porque sin él todo
responde `426 Upgrade Required`).

**Alcance:** las 47 fechas de la sección 7.0/9 (2026-07-12 → 2026-08-30, con el hueco ya conocido
del 24-26 de julio sin datos). Se llamó `POST /dev/other-concepts/run-snapshot?fecha=X` una vez por
fecha, en 7 tandas (1 de prueba + 6 lotes de hasta 8 fechas), todas secuenciales, ninguna en
paralelo.

**Resultado — las 47 llamadas terminaron `paginasConError: 0`:**

| Fecha | Créditos procesados | jobExecutionId |
|---|---|---|
| 2026-08-06 (prueba) | 398 | `428bbdde-5c74-4050-90df-d954a5bf9b7b` |
| 2026-07-12 | 398 | `99a52c66-023b-458b-a671-30819935b172` |
| 2026-07-13 | 398 | `2ec16794-2dd0-4d29-812c-51c1c2e37f0d` |
| 2026-07-14 | 398 | `dee011bf-7409-43b2-9650-13d06b6dbc4d` |
| 2026-07-15 | 398 | `5786a24d-4b3b-4b36-86d1-ae74df84e0b8` |
| 2026-07-16 | 398 | `224e981f-b1e7-4d85-90de-0cf860ecb57a` |
| 2026-07-17 | 398 | `c93ed29e-0090-4319-8bef-8f3dbbd8c737` |
| 2026-07-18 | 398 | `ef845bcc-c48f-4264-99c2-b61a8c6d2844` |
| 2026-07-19 | 398 | `7ac8870a-71ff-4bee-8acd-ebcb0075d436` |
| 2026-07-20 | 398 | `a520fc74-6795-4556-8f13-6295470d35b5` |
| 2026-07-21 | 398 | `3fac8be9-cc0d-4d9e-9d7b-5086a491d11e` |
| 2026-07-22 | 398 | `da05ff71-a511-4233-a6a2-192fe9338290` |
| 2026-07-23 | 398 | `274dc84b-e9ef-4f1a-ac28-ebea67e4f4ed` |
| 2026-07-27 | 398 | `f06de01c-391c-4c92-9b2f-f7c94e8c2475` |
| 2026-07-28 | 398 | `85490ba7-2558-4fd0-b4ac-21b3f7a37aaa` |
| 2026-07-29 | 398 | `e0964e5c-1840-41ba-bd7f-0b6a70460092` |
| 2026-07-30 | 398 | `70a6f63e-6878-4b6f-adec-011636a5b9b2` |
| 2026-07-31 | 398 | `eb73777c-d329-4f0e-861f-510579fbe3da` |
| 2026-08-01 | 398 | `23fc6f26-5904-45e3-9f94-37d061656d28` |
| 2026-08-02 | 398 | `d38d485a-55d6-4d6e-a259-61ce780cb45a` |
| 2026-08-03 | 398 | `2a716649-48ce-431d-902e-c240fdfa2b27` |
| 2026-08-04 | 398 | `62d30f9e-4496-446d-8ff0-cc5727245270` |
| 2026-08-05 | 398 | `6161412c-1d7d-4ef4-b538-1b21df0ddaae` |
| 2026-08-07 | 398 | `a2782075-0b17-4317-a422-31dabb9f1d73` |
| 2026-08-08 | 398 | `ba941373-aee9-4ed0-88b1-d495971d4226` |
| 2026-08-09 | 398 | `d6d836f3-8485-42b1-b4c3-733259e8b2f8` |
| 2026-08-10 | 398 | `3cb76877-b0b5-437c-bafb-9465277d7fb6` |
| 2026-08-11 | 398 | `5d55aa83-1ebe-45ab-a0b4-ba714fb2c257` |
| 2026-08-12 | 398 | `a8795651-0bf7-48f4-a9e0-ff5ad5f0d756` |
| 2026-08-13 | 398 | `8458be86-b527-45a9-897d-12b925e79932` |
| 2026-08-14 | 398 | `d99e5659-4ea1-40f6-bcb2-a3d76e1f6441` |
| 2026-08-15 | 398 | `1caa806a-8a5c-484f-a194-560af44a12f5` |
| 2026-08-16 | 398 | `d64c9dcc-4c96-4fdc-9e4b-16ea7f0cf3d4` |
| 2026-08-17 | 398 | `989a7e3a-638c-4c50-9233-18b94db6f508` |
| 2026-08-18 | 398 | `70464068-6a69-4ba2-bb89-336b7b6979e5` |
| 2026-08-19 | 398 | `96bc390b-3d1b-4762-82ad-44c7f00cb17d` |
| 2026-08-20 | 398 | `451b49be-94c9-4e84-8453-eaee6c7590f7` |
| 2026-08-21 | 398 | `a7f64037-19c3-47f0-a906-4ec7deedaa6d` |
| 2026-08-22 | 398 | `f5543007-a5c3-4cf6-a55b-4af997faf259` |
| 2026-08-23 | 398 | `3fe83573-937a-4c43-bdb1-f6eca5b48213` |
| 2026-08-24 | 398 | `6b7edad7-e32b-427f-b1f3-e0854db0ce5e` |
| 2026-08-25 | 398 | `66c01a2a-0029-4af8-8860-533084c9426a` |
| 2026-08-26 | 398 | `8de72c0d-9895-4b79-a2be-7da5f387c172` |
| 2026-08-27 | 398 | `e4e38b2f-6bd9-4287-839c-218080eb4748` |
| 2026-08-28 | 398 | `d67802f9-fa41-405e-ab92-1524e5e9c4ff` |
| 2026-08-29 | 398 | `2d3ed404-3e45-4631-987b-4bbfee61c3dd` |
| 2026-08-30 | 398 | `f0c7b514-bb2e-491b-b3c0-3fa87fb7eaeb` |

Nota: `398` es el conteo global de créditos `ACTIVE` en el momento de la corrida (2026-09-05) —
igual en las 47 fechas porque, como dice la limitación de la sección 7 punto 3, el job usa el
conjunto de créditos activo **actual**, no el histórico de cada fecha.

**Verificación post-regeneración (vía `GET /portfolio-snapshots/get-by-date/{fecha}`, sumando en
Python por no tener cliente `mysql` a mano en ese momento):**

| Fecha | Créditos zona ELITE | saldoTotal ELITE | capitalPendiente ELITE |
|---|---|---|---|
| 2026-08-06 (ya regenerado) | 77 | $3.544.021,23 | $3.759.559,63 |
| 2026-09-03 | 77 | $26.726.788,55 | $19.805.067,41 |

Antes de la regeneración, el 06-ago mostraba `saldoTotal ≈ $269.355.353,08` (con 74 créditos) — una
caída de ~90% hacia el 03-sep que no tenía sentido de negocio. Después de regenerar, el saldo **crece**
de $3,5M a $26,7M entre esas fechas, que es el comportamiento esperado de una cartera activa
acumulando cuotas vencidas con el tiempo. El conteo de créditos también pasó a ser el mismo en ambas
fechas (77) por la razón explicada en la nota de arriba.

**Hallazgo nuevo, no corregido, para seguimiento aparte:** en créditos sin ninguna cuota vencida
todavía a la fecha de corte (ej. crédito 691 al 2026-08-06, cuya primera cuota vence el 18-ago),
`totalCuotas`, `cuotasPagadas` y `cuotasPendientes` salen `null` en vez de `0`. Pasa porque la CTE
`cuotas` de `CreditSnapshotSourceRepository` filtra por `expiration_date <= :fecha`, y si ningún
registro de `credit_amortization` del crédito cumple esa condición, el `GROUP BY` no genera fila
para ese `credit_id` — el `LEFT JOIN` en la query principal deja esas tres columnas en `NULL` en vez
de `0`. No afecta ningún cálculo de saldo (es cosmético a nivel de esas tres columnas), pero si algún
consumidor (frontend, reporte) no maneja `null` con cuidado, puede romper una suma o un `.toFixed()`.
Pendiente de decidir si se corrige (ej. `COALESCE` en esas tres columnas del `SELECT` principal).

## 11. Bitácora — regeneración ejecutada en PRODUCCIÓN (2026-09-06)

Segunda corrida, esta vez contra la base de **producción** (`159.203.163.185/recaudo`). La corrida de
la sección 10 fue contra `recaudo_dev`; esta la replica en producción, con dos diferencias de alcance
importantes que se descubrieron al hacer el levantamiento previo.

**Ambiente:** backend local (`./gradlew bootRun`, sin perfil activo), con `application.yml` apuntando
a `159.203.163.185/recaudo`. Se corrió **sin** el perfil `scheduler` a propósito: `application.yml`
tiene los tres crons en `"0 0 0 31 2 ?"` (31 de febrero = nunca), mientras que
`application-scheduler.yml` los tiene reales (mora 7am, debido-cobrar 7am, snapshot 11pm). Levantar
con el perfil `scheduler` para llegar a la BD de producción habría dejado **armados** los jobs de
mora y debido-cobrar contra producción durante toda la corrida. Usuario `administrador`, header
`X-App-Version: 1.0.3` en cada request.

### 11.1 Hallazgo previo: producción corría el código viejo

A diferencia de dev —donde el fix `233bbd0` estaba aplicado desde el 2026-09-01 y por tanto los
snapshots de septiembre ya eran correctos— en producción **nunca se desplegó**. Evidencia recogida
antes de escribir nada, sobre el snapshot del 2026-09-05 generado por el cron de producción:

- `capitalGenerado = 0` en **0 de 403** créditos.
- `totalCuotas NULL` en **0 de 403**.

Bajo la query corregida, todo crédito sin cuota vencida a la fecha de corte debe dar `capitalGenerado
= 0` **y** `totalCuotas NULL` (el bug de la sección 10). Producción pasó de 398 a 403 créditos entre
el 01 y el 05 de septiembre, así que forzosamente había créditos recién originados que caerían en ese
caso. Cero ocurrencias ⇒ código viejo.

Corroborado por la serie de `saldoTotal`, que iba de $720,4 M (12-jul) a $869,1 M (05-sep) — casi
plana en dos meses. Bajo la query corregida ese número debe **crecer** con el tiempo, porque van
venciendo cuotas.

**Consecuencia sobre el alcance:** se regeneraron **53 fechas** (2026-07-12 → 2026-09-05), no las 47
de dev. En producción también septiembre estaba afectado.

### 11.2 Corrección aplicada antes de correr

Se agregó el `COALESCE` pendiente de la sección 10 (`totalCuotas`, `cuotasPagadas`, `cuotasPendientes`
salían `NULL` en créditos sin cuota vencida), en `CreditSnapshotSourceRepository.java` líneas 97-99:

```sql
COALESCE(cuotas.total_cuotas, 0)   AS totalCuotas,
COALESCE(cuotas.cuotas_pagadas, 0) AS cuotasPagadas,
(COALESCE(cuotas.total_cuotas, 0) - COALESCE(cuotas.cuotas_pagadas, 0)) AS cuotasPendientes,
```

Se hizo antes de la corrida para no tener que regenerar las 53 fechas de producción una segunda vez.
Validado: **0 NULLs** en las 53 fechas después de regenerar.

También se revirtió un cambio accidental en `application.yml` que dejaba
`spring.servlet.multipart.file-size-threshold` **vacío** (antes `2KB`). Un valor vacío no convierte a
`DataSize` y habría impedido el arranque.

### 11.3 Respaldo previo

Además del backup de BD tomado por el equipo, se guardó el estado completo de las 53 fechas vía
`GET /portfolio-snapshots/get-by-date/{fecha}`, un JSON por fecha (~720 KB c/u) más un CSV de
totales. Es una segunda vía de recuperación, independiente del backup de motor.

### 11.4 Resultado

**53 de 53 fechas en `OK`, `paginasConError: 0`, cero fallos.** 403 créditos procesados en cada una
(el conteo global de `ACTIVE` al momento de la corrida — igual en todas las fechas por la limitación
de la sección 7 punto 3). Duración ~57 s por fecha, ~50 min en total. Se ejecutó una fecha de prueba
(2026-08-06), se verificó contra el respaldo, y luego el resto en un lote secuencial con registro
incremental por fecha.

Antes/después de la fecha de prueba:

| Campo | Antes | Después |
|---|---|---|
| créditos | 414 | 403 |
| `capitalGenerado` | $901.124.365 | $283.523.479 |
| `capitalPagado` | $363.595.936 | $212.412.769 |
| `saldoTotal` | $803.637.143 | $101.148.872 |
| créditos con `capitalGenerado = 0` | 0 | 150 |
| `totalCuotas` NULL | 0 | 0 |

Verificación agregada sobre las 53 fechas:

- 53/53 con 403 créditos.
- **0 NULLs** en `totalCuotas` (confirma 11.2).
- `saldoTotal` pasó de una serie casi plana en ~$720-869 M a una serie creciente de **$66,9 M
  (12-jul) a $152,1 M (05-sep)** — el comportamiento esperado de una cartera activa acumulando
  cuotas vencidas.
- Créditos con `capitalGenerado = 0` bajan de **236 (12-jul) a 35 (05-sep)** de forma casi monótona:
  a medida que avanza el tiempo, menos créditos están sin ninguna cuota vencida. Es un chequeo de
  consistencia interna fuerte.

La serie de saldo **no** es monótona creciente y no debe serlo: baja en días de recaudo alto, cuando
lo cobrado supera lo que venció. Ej. 08-11 ($108,5 M) → 08-12 ($107,6 M).

### 11.5 Pendientes que esta corrida NO resuelve

1. **⚠️ El deploy del código corregido a producción sigue pendiente.** Mientras no se despliegue
   `233bbd0` o posterior (incluyendo el `COALESCE` de 11.2), el cron de las 23:00 sigue generando
   cada noche con la query vieja. El histórico quedó bien; lo nuevo nace mal desde el 2026-09-06.
   **Al desplegar, hay que regenerar las fechas transcurridas entre esta corrida y el deploy.**
2. `estadoCredito`, `cuotasPagadas` y `diasMora` siguen reflejando el estado **actual**, no el de
   cada fecha pasada (limitación de la sección 7 punto 1). No cambia con regenerar.
3. El conteo de créditos es el `ACTIVE` actual en todas las fechas, no el histórico de cada una
   (sección 7 punto 3).
4. `otrosConceptosGenerado` sigue en 0 fijo (sección 7 punto 2).
5. **⚠️ Hallazgo nuevo — saldos negativos por pagos anticipados.** La query corregida cuenta lo
   *generado* con `ca.expiration_date <= :fecha` pero lo *pagado* con `nr.created_at < :fecha + 1 día`.
   Un cliente que abona **antes** de que la cuota venza produce `pagado > generado` y por tanto un
   `*Pendiente` negativo. Con la query vieja no ocurría, porque `generado` era el plan completo y el
   pago nunca lo superaba.

   Medido en producción tras la regeneración:

   | Fecha | Créditos con `saldoTotal < 0` | Suma de negativos | `saldoTotal` de la fecha |
   |---|---|---|---|
   | 2026-07-12 | 14 | −$11.125.658,73 | $66.918.218,48 |
   | 2026-08-06 | 28 (antes de regenerar: 1) | −$12.023.335,40 | $101.148.871,87 |
   | 2026-09-05 | 42 | −$7.803.609,82 | $152.103.600,64 |

   Caso testigo: crédito **590** al 2026-08-06 — `capitalGenerado = 0` (ninguna cuota vencida a esa
   fecha), `capitalPagado = 81.000`, `saldoTotal = −150.000`.

   **No es un error de la regeneración** — es inherente al fix de la fecha de corte, y aritméticamente
   la fila es consistente. Pero **es una decisión de negocio pendiente**: hoy esos montos negativos
   **restan** del saldo agregado de cartera (zona, cliente, global), como si redujeran lo que deben los
   demás deudores. Un cliente con pago anticipado no debería disminuir la cartera por cobrar de otros.

   Opciones a evaluar con el área de cartera, ninguna aplicada:
   - Truncar en 0 los `*Pendiente` negativos y exponer el excedente en un campo aparte
     (`saldoAFavor`), que es lo que representa de verdad.
   - Dejarlos negativos a nivel de crédito pero excluirlos de las sumas agregadas.
   - Dejarlo como está, documentando que `saldoTotal` es un neto que incluye anticipos.

   Cualquiera de las dos primeras implica cambiar `CreditSnapshotSourceRepository` y **regenerar otra
   vez** las fechas afectadas.
