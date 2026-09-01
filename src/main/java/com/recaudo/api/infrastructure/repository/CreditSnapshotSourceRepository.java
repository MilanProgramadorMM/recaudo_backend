package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.CreditSnapshotProjection;
import com.recaudo.api.domain.model.entity.CreditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditSnapshotSourceRepository extends JpaRepository<CreditEntity, Integer> {

    // Bandera countQuery necesaria porque la query base tiene múltiples JOINs/subqueries

    @Query(
            value = """
        WITH
        cuotas AS (
            SELECT ca.credit_id,
                   COUNT(*) AS total_cuotas,
                   SUM(CASE WHEN ca.paid_full = 'S' THEN 1 ELSE 0 END) AS cuotas_pagadas
            FROM credit_amortization ca
            WHERE ca.expiration_date <= :fecha
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
            WHERE ca.expiration_date <= :fecha
              AND cad.concept_id IN (48, 49, 50, 51)
            GROUP BY ca.credit_id
        ),
        pagado AS (
            SELECT nr.credit_id,
                   SUM(CASE WHEN nrd.concept_id = 48 THEN ABS(nrd.value) ELSE 0 END) AS capital,
                   SUM(CASE WHEN nrd.concept_id = 49 THEN ABS(nrd.value) ELSE 0 END) AS interes,
                   SUM(CASE WHEN nrd.concept_id = 50 THEN ABS(nrd.value) ELSE 0 END) AS seg_vida,
                   SUM(CASE WHEN nrd.concept_id = 51 THEN ABS(nrd.value) ELSE 0 END) AS seg_cartera,
                   SUM(CASE WHEN nrd.concept_id = 53 THEN ABS(nrd.value) ELSE 0 END) AS mora_pago_53,
                   SUM(CASE WHEN nrd.concept_id = 54 THEN ABS(nrd.value) ELSE 0 END) AS mora_pago_54
            FROM new_recaudo nr
            JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
            WHERE nr.value_paid < 0
              AND nr.created_at < (:fecha + INTERVAL 1 DAY)
              AND nrd.concept_id IN (48, 49, 50, 51, 53, 54)
            GROUP BY nr.credit_id
        ),
        mora_gen AS (
            SELECT coc.credit_id,
                   SUM(cocd.value) AS generado
            FROM credit_other_concepts coc
            JOIN credit_other_concepts_detail cocd ON cocd.credit_other_concepts_id = coc.id
            WHERE cocd.concept_id = 52
              AND cocd.created_at < (:fecha + INTERVAL 1 DAY)
            GROUP BY coc.credit_id
        ),
        mora_dias AS (
            SELECT ca.credit_id,
                   MIN(ca.expiration_date) AS fecha_vencimiento_mas_antigua
            FROM credit_amortization ca
            WHERE ca.paid_full <> 'S'
              AND ca.expiration_date < :fecha
            GROUP BY ca.credit_id
        )
        SELECT
            c.id                             AS creditId,
            c.person_id                      AS personId,
            p.fullname                       AS cliente,
            p.document                       AS documento,
            ci.zone_id                       AS zonaId,
            z.value                          AS zona,
            c.credit_status                  AS estadoCredito,
            cl.id                            AS creditLineId,
            cl.name                          AS creditLineNombre,
            pe.id                            AS periodId,
            pe.name                          AS periodNombre,
            pe.cod                           AS periodCodigo,
            tt.id                            AS taxTypeId,
            tt.name                          AS taxTypeNombre,
            c.tax_value                      AS taxValue,
            c.period_quantity                AS cuotasPlaneadas,
            cuotas.total_cuotas              AS totalCuotas,
            cuotas.cuotas_pagadas            AS cuotasPagadas,
            (cuotas.total_cuotas - cuotas.cuotas_pagadas) AS cuotasPendientes,
            ROUND(COALESCE(generado.capital, 0), 2)  AS capitalGenerado,
            ROUND(COALESCE(pagado.capital, 0), 2)    AS capitalPagado,
            ROUND(COALESCE(generado.capital, 0) - COALESCE(pagado.capital, 0), 2) AS capitalPendiente,
            ROUND(COALESCE(generado.interes, 0), 2)  AS interesGenerado,
            ROUND(COALESCE(pagado.interes, 0), 2)    AS interesPagado,
            ROUND(COALESCE(generado.interes, 0) - COALESCE(pagado.interes, 0), 2) AS interesPendiente,
            ROUND(COALESCE(generado.seg_vida, 0), 2) AS seguroVidaGenerado,
            ROUND(COALESCE(pagado.seg_vida, 0), 2)   AS seguroVidaPagado,
            ROUND(COALESCE(generado.seg_vida, 0) - COALESCE(pagado.seg_vida, 0), 2) AS seguroVidaPendiente,
            ROUND(COALESCE(generado.seg_cartera, 0), 2) AS seguroCarteraGenerado,
            ROUND(COALESCE(pagado.seg_cartera, 0), 2)   AS seguroCarteraPagado,
            ROUND(COALESCE(generado.seg_cartera, 0) - COALESCE(pagado.seg_cartera, 0), 2) AS seguroCarteraPendiente,
            ROUND(COALESCE(mora_gen.generado, 0), 2) AS moraGenerada,
            ROUND(COALESCE(pagado.mora_pago_53, 0) - COALESCE(pagado.mora_pago_54, 0), 2) AS moraPagada,
            ROUND(COALESCE(mora_gen.generado, 0)
                - (COALESCE(pagado.mora_pago_53, 0) - COALESCE(pagado.mora_pago_54, 0)), 2) AS moraPendiente,
            ROUND(
                COALESCE(pagado.capital, 0) + COALESCE(pagado.interes, 0)
              + COALESCE(pagado.seg_vida, 0) + COALESCE(pagado.seg_cartera, 0)
              + (COALESCE(pagado.mora_pago_53, 0) - COALESCE(pagado.mora_pago_54, 0))
            , 2) AS totalPagado,
            ROUND(
                (COALESCE(generado.capital, 0)     - COALESCE(pagado.capital, 0))
              + (COALESCE(generado.interes, 0)     - COALESCE(pagado.interes, 0))
              + (COALESCE(generado.seg_vida, 0)    - COALESCE(pagado.seg_vida, 0))
              + (COALESCE(generado.seg_cartera, 0) - COALESCE(pagado.seg_cartera, 0))
              + (COALESCE(mora_gen.generado, 0)
                    - (COALESCE(pagado.mora_pago_53, 0) - COALESCE(pagado.mora_pago_54, 0)))
            , 2) AS saldoTotal,
            ROUND(0, 2) AS otrosConceptosGenerado,
            COALESCE(DATEDIFF(:fecha, mora_dias.fecha_vencimiento_mas_antigua), 0) AS diasMora
        FROM credit c
        INNER JOIN person p            ON p.id = c.person_id
        INNER JOIN credit_intention ci ON ci.id = c.credit_intention_id
        LEFT  JOIN zona z              ON z.id = ci.zone_id
        LEFT  JOIN credit_line cl      ON cl.id = c.credit_line_id
        LEFT  JOIN period pe           ON pe.id = c.period_id
        LEFT  JOIN tax_type tt         ON tt.id = c.tax_type_id
        LEFT  JOIN cuotas              ON cuotas.credit_id    = c.id
        LEFT  JOIN generado            ON generado.credit_id  = c.id
        LEFT  JOIN pagado              ON pagado.credit_id    = c.id
        LEFT  JOIN mora_gen            ON mora_gen.credit_id  = c.id
        LEFT  JOIN mora_dias           ON mora_dias.credit_id = c.id
        WHERE c.credit_status = 'ACTIVE'
        ORDER BY c.id
        """,
            countQuery = "SELECT COUNT(*) FROM credit c WHERE c.credit_status = 'ACTIVE'",
            nativeQuery = true
    )
    Page<CreditSnapshotProjection> findActiveCreditsSnapshot(@Param("fecha") java.sql.Date fecha, Pageable pageable);
}