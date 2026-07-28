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
            SELECT
                c.id                            AS creditId,
                c.person_id                     AS personId,
                p.fullname                      AS cliente,
                p.document                      AS documento,
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
                cuotas.total_cuotas               AS totalCuotas,
                cuotas.cuotas_pagadas             AS cuotasPagadas,
                (cuotas.total_cuotas - cuotas.cuotas_pagadas) AS cuotasPendientes,

                ROUND(COALESCE(capital.generado, 0), 2)        AS capitalGenerado,
                ROUND(COALESCE(capital.pagado, 0), 2)          AS capitalPagado,
                ROUND(COALESCE(capital.generado, 0) - COALESCE(capital.pagado, 0), 2) AS capitalPendiente,

                ROUND(COALESCE(interes.generado, 0), 2)        AS interesGenerado,
                ROUND(COALESCE(interes.pagado, 0), 2)          AS interesPagado,
                ROUND(COALESCE(interes.generado, 0) - COALESCE(interes.pagado, 0), 2) AS interesPendiente,

                ROUND(COALESCE(seg_vida.generado, 0), 2)       AS seguroVidaGenerado,
                ROUND(COALESCE(seg_vida.pagado, 0), 2)         AS seguroVidaPagado,
                ROUND(COALESCE(seg_vida.generado, 0) - COALESCE(seg_vida.pagado, 0), 2) AS seguroVidaPendiente,

                ROUND(COALESCE(seg_cartera.generado, 0), 2)    AS seguroCarteraGenerado,
                ROUND(COALESCE(seg_cartera.pagado, 0), 2)      AS seguroCarteraPagado,
                ROUND(COALESCE(seg_cartera.generado, 0) - COALESCE(seg_cartera.pagado, 0), 2) AS seguroCarteraPendiente,

                ROUND(COALESCE(mora.generado, 0), 2)           AS moraGenerada,
                ROUND(COALESCE(mora.pagado, 0), 2)             AS moraPagada,
                ROUND(COALESCE(mora.generado, 0) - COALESCE(mora.pagado, 0), 2) AS moraPendiente,

                ROUND(
                    COALESCE(capital.pagado, 0)
                  + COALESCE(interes.pagado, 0)
                  + COALESCE(seg_vida.pagado, 0)
                  + COALESCE(seg_cartera.pagado, 0)
                  + COALESCE(mora.pagado, 0)
                , 2) AS totalPagado,

                ROUND(
                    (COALESCE(capital.generado,0)     - COALESCE(capital.pagado,0))
                  + (COALESCE(interes.generado,0)     - COALESCE(interes.pagado,0))
                  + (COALESCE(seg_vida.generado,0)    - COALESCE(seg_vida.pagado,0))
                  + (COALESCE(seg_cartera.generado,0) - COALESCE(seg_cartera.pagado,0))
                  + (COALESCE(mora.generado,0)        - COALESCE(mora.pagado,0))
                , 2) AS saldoTotal,
                            
                ROUND(COALESCE(otros.generado, 0), 2)          AS otrosConceptosGenerado,

                COALESCE(DATEDIFF(:fecha, mora_dias.fecha_vencimiento_mas_antigua), 0) AS diasMora

            FROM credit c
            INNER JOIN person p            ON p.id = c.person_id
            INNER JOIN credit_intention ci ON ci.id = c.credit_intention_id
            LEFT  JOIN zona z              ON z.id = ci.zone_id
            LEFT  JOIN credit_line cl      ON cl.id = c.credit_line_id
            LEFT  JOIN period pe           ON pe.id = c.period_id
            LEFT  JOIN tax_type tt         ON tt.id = c.tax_type_id

            LEFT JOIN (
                SELECT credit_id,
                       COUNT(*) AS total_cuotas,
                       SUM(CASE WHEN paid_full = 'S' THEN 1 ELSE 0 END) AS cuotas_pagadas
                FROM credit_amortization
                GROUP BY credit_id
            ) cuotas ON cuotas.credit_id = c.id

            LEFT JOIN (
                SELECT ca.credit_id, SUM(cad.value) AS generado,
                       (SELECT COALESCE(SUM(ABS(nrd.value)), 0)
                        FROM new_recaudo nr JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
                        WHERE nr.credit_id = ca.credit_id AND nrd.concept_id = 48) AS pagado
                FROM credit_amortization ca
                JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
                WHERE cad.concept_id = 48
                GROUP BY ca.credit_id
            ) capital ON capital.credit_id = c.id

            LEFT JOIN (
                SELECT ca.credit_id, SUM(cad.value) AS generado,
                       (SELECT COALESCE(SUM(ABS(nrd.value)), 0)
                        FROM new_recaudo nr JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
                        WHERE nr.credit_id = ca.credit_id AND nrd.concept_id = 49) AS pagado
                FROM credit_amortization ca
                JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
                WHERE cad.concept_id = 49
                GROUP BY ca.credit_id
            ) interes ON interes.credit_id = c.id

            LEFT JOIN (
                SELECT ca.credit_id, SUM(cad.value) AS generado,
                       (SELECT COALESCE(SUM(ABS(nrd.value)), 0)
                        FROM new_recaudo nr JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
                        WHERE nr.credit_id = ca.credit_id AND nrd.concept_id = 50) AS pagado
                FROM credit_amortization ca
                JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
                WHERE cad.concept_id = 50
                GROUP BY ca.credit_id
            ) seg_vida ON seg_vida.credit_id = c.id

            LEFT JOIN (
                SELECT ca.credit_id, SUM(cad.value) AS generado,
                       (SELECT COALESCE(SUM(ABS(nrd.value)), 0)
                        FROM new_recaudo nr JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
                        WHERE nr.credit_id = ca.credit_id AND nrd.concept_id = 51) AS pagado
                FROM credit_amortization ca
                JOIN credit_amortization_detail cad ON cad.amortization_id = ca.id
                WHERE cad.concept_id = 51
                GROUP BY ca.credit_id
            ) seg_cartera ON seg_cartera.credit_id = c.id

            LEFT JOIN (
                SELECT coc.credit_id,
                       (SELECT COALESCE(SUM(cocd2.value), 0)
                        FROM credit_other_concepts_detail cocd2
                        JOIN credit_other_concepts coc2 ON coc2.id = cocd2.credit_other_concepts_id
                        WHERE coc2.credit_id = coc.credit_id AND cocd2.concept_id = 52) AS generado,
                       (SELECT COALESCE(SUM(ABS(nrd.value)), 0)
                        FROM new_recaudo nr JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
                        WHERE nr.credit_id = coc.credit_id AND nrd.concept_id = 53)
                       -
                       (SELECT COALESCE(SUM(ABS(nrd.value)), 0)
                        FROM new_recaudo nr JOIN new_recaudo_detail nrd ON nrd.recaudo_id = nr.id
                        WHERE nr.credit_id = coc.credit_id AND nrd.concept_id = 54) AS pagado
                FROM credit_other_concepts coc
                GROUP BY coc.credit_id
            ) mora ON mora.credit_id = c.id

            LEFT JOIN (
                SELECT credit_id, MIN(expiration_date) AS fecha_vencimiento_mas_antigua
                FROM credit_amortization
                WHERE paid_full <> 'S' AND expiration_date < :fecha
                GROUP BY credit_id
            ) mora_dias ON mora_dias.credit_id = c.id
                
            LEFT JOIN (
                SELECT
                    coc.
                credit_id,
                    SUM(cocd.value) AS generado
                FROM credit_other_concepts coc
                JOIN credit_other_concepts_detail cocd
                ON cocd.credit_other_concepts_id = coc.id
                WHERE cocd.
                concept_id NOT IN (52,53,54)
                GROUP BY coc.credit_id
            )
                otros ON otros.credit_id = c.id

            WHERE c.credit_status = 'ACTIVE'
            ORDER BY c.id
            """,
        countQuery = "SELECT COUNT(*) FROM credit c WHERE c.credit_status = 'ACTIVE'",
        nativeQuery = true
    )
    Page<CreditSnapshotProjection> findActiveCreditsSnapshot(@Param("fecha") java.sql.Date fecha, Pageable pageable);
}