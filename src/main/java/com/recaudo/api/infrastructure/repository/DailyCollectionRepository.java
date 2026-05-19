package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.domain.model.dto.response.DailyCollectionRespaldoProjection;
import com.recaudo.api.domain.model.entity.AmortizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCollectionRepository
        extends JpaRepository<AmortizationEntity, Long> {


    @Query(value = """
    SELECT DISTINCT
         c.id                                                        AS creditId,
         c.created_at                                                AS fechaCredito,
         cl.name                                                   AS lineaname,
         c.total_capital_value                                   AS totalCapitalValue,
         a.id                                                        AS cuotaId,
         a.quota_number                                              AS quotaNumber,
         a.expiration_date                                           AS expirationDate,
         ci.fullname                                                 AS clientName,
         pz_cliente.orden                                            AS clientOrden,
         a.total_quota_value                                         AS clientCuota,
         z.description                                               AS zona,
         COALESCE(cv.paid, 0)                                        AS paidToday,
         a.paid_full                                                 AS paidFull,
         a.liquidated                                                AS liquidated,
         cv.payment_promise_date                                     AS paymentPromiseDate,
         COALESCE(cv.no_pago, 0)                                     AS noPago,
         cv.no_pago_reason                                           AS noPagoReason,
         p.cod                                                       AS periodo,
         c.period_quantity                                           AS plazoCredito,
         a.total_quota_value                                         AS valorCuota,
         (SELECT expiration_date
          FROM credit_amortization a0
          WHERE a0.credit_id = c.id
          ORDER BY quota_number DESC
          LIMIT 1
         )                                                           AS fecha_vence,
         (SELECT COUNT(1)
          FROM credit_amortization a3
          WHERE a3.credit_id = c.id
            AND a3.paid_full = 'S')                                  AS cuotasPagadas,
         (SELECT COUNT(1)
          FROM credit_amortization a4
          WHERE a4.credit_id = c.id
            AND a4.paid_full = 'N'
            AND a4.expiration_date < CURDATE())                      AS cuotasVencidas,
         (
             SELECT COALESCE(SUM(
                 COALESCE((
                     SELECT SUM(cad.value)
                     FROM credit_amortization_detail cad
                     JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'VIV'
                     WHERE cad.amortization_id = a5.id
                 ), 0)
                 - COALESCE((
                     SELECT ABS(SUM(nrd.value))
                     FROM new_recaudo_detail nrd
                     JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a5.id
                     JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'VIV'
                 ), 0)
                 + COALESCE((
                     SELECT SUM(cad.value)
                     FROM credit_amortization_detail cad
                     JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'VIT'
                     WHERE cad.amortization_id = a5.id
                 ), 0)
                 - COALESCE((
                     SELECT ABS(SUM(nrd.value))
                     FROM new_recaudo_detail nrd
                     JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a5.id
                     JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'VIT'
                 ), 0)
                 + COALESCE((
                     SELECT SUM(cad.value)
                     FROM credit_amortization_detail cad
                     JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'SV'
                     WHERE cad.amortization_id = a5.id
                 ), 0)
                 - COALESCE((
                     SELECT ABS(SUM(nrd.value))
                     FROM new_recaudo_detail nrd
                     JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a5.id
                     JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'SV'
                 ), 0)
                 + COALESCE((
                     SELECT SUM(cad.value)
                     FROM credit_amortization_detail cad
                     JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'SC'
                     WHERE cad.amortization_id = a5.id
                 ), 0)
                 - COALESCE((
                     SELECT ABS(SUM(nrd.value))
                     FROM new_recaudo_detail nrd
                     JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a5.id
                     JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'SC'
                 ), 0)
                 + COALESCE((
                     SELECT SUM(cod.value)
                     FROM credit_other_concepts_detail cod
                     JOIN credit_other_concepts coc ON coc.id = cod.credit_other_concepts_id
                     JOIN glotypes g ON g.id = cod.concept_id AND g.code = 'IMT'
                     WHERE coc.credit_id    = a5.credit_id
                       AND coc.quota_number = a5.quota_number
                       AND cod.deleted_at IS NULL
                 ), 0)
                 - COALESCE((
                     SELECT ABS(SUM(nrd.value))
                     FROM new_recaudo_detail nrd
                     JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a5.id
                     JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'IMT'
                 ), 0)
             ), 0)
             FROM credit_amortization a5
             WHERE a5.credit_id = c.id
               AND a5.paid_full = 'N'
         )                                                           AS saldoPendiente,
    
         -- ── Saldo pendiente SOLO de la cuota actual ────────────────────────
         COALESCE(
             -- VIV: Capital esperado menos lo pagado
             COALESCE((
                 SELECT SUM(cad.value)
                 FROM credit_amortization_detail cad
                 JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'VIV'
                 WHERE cad.amortization_id = a.id
             ), 0)
             - COALESCE((
                 SELECT ABS(SUM(nrd.value))
                 FROM new_recaudo_detail nrd
                 JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a.id
                 JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'VIV'
             ), 0)
             -- VIT: Interés esperado menos lo pagado
             + COALESCE((
                 SELECT SUM(cad.value)
                 FROM credit_amortization_detail cad
                 JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'VIT'
                 WHERE cad.amortization_id = a.id
             ), 0)
             - COALESCE((
                 SELECT ABS(SUM(nrd.value))
                 FROM new_recaudo_detail nrd
                 JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a.id
                 JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'VIT'
             ), 0)
             -- SV: Seguro vida esperado menos lo pagado
             + COALESCE((
                 SELECT SUM(cad.value)
                 FROM credit_amortization_detail cad
                 JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'SV'
                 WHERE cad.amortization_id = a.id
             ), 0)
             - COALESCE((
                 SELECT ABS(SUM(nrd.value))
                 FROM new_recaudo_detail nrd
                 JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a.id
                 JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'SV'
             ), 0)
             -- SC: Seguro cartera esperado menos lo pagado
             + COALESCE((
                 SELECT SUM(cad.value)
                 FROM credit_amortization_detail cad
                 JOIN glotypes g ON g.id = cad.concept_id AND g.code = 'SC'
                 WHERE cad.amortization_id = a.id
             ), 0)
             - COALESCE((
                 SELECT ABS(SUM(nrd.value))
                 FROM new_recaudo_detail nrd
                 JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a.id
                 JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'SC'
             ), 0)
             -- IMT: Mora causada menos mora pagada
             + COALESCE((
                 SELECT SUM(cod.value)
                 FROM credit_other_concepts_detail cod
                 JOIN credit_other_concepts coc ON coc.id = cod.credit_other_concepts_id
                 JOIN glotypes g ON g.id = cod.concept_id AND g.code = 'IMT'
                 WHERE coc.credit_id    = a.credit_id
                   AND coc.quota_number = a.quota_number
                   AND cod.deleted_at IS NULL
             ), 0)
             - COALESCE((
                 SELECT ABS(SUM(nrd.value))
                 FROM new_recaudo_detail nrd
                 JOIN new_recaudo nr ON nr.id = nrd.recaudo_id AND nr.quota_id = a.id
                 JOIN glotypes g ON g.id = nrd.concept_id AND g.code = 'IMT'
             ), 0)
         , 0)                                                        AS saldoPendienteCuota,
    
         (SELECT COUNT(1)
          FROM credit_amortization a6
          WHERE a6.credit_id = c.id)                                 AS totalCuotas,
         (SELECT ci2.value
          FROM contact_info ci2
          WHERE ci2.person = c.person_id
            AND ci2.type = 10
            AND ci2.deleted_at IS NULL LIMIT 1)                      AS direccion,
         (SELECT ci3.value
          FROM contact_info ci3
          WHERE ci3.person = c.person_id
            AND ci3.type = 13
            AND ci3.deleted_at IS NULL LIMIT 1)                      AS telefono,
         (SELECT b.value
          FROM barrio b
          WHERE b.id = ci.neighborhood_id LIMIT 1)                  AS barrio,
         (SELECT m.value
          FROM municipio m
          WHERE m.id = ci.municipality_id LIMIT 1)                  AS municipio,
         DAYNAME(a.expiration_date)                                  AS nombreDia,
         COALESCE((
             SELECT SUM(cod.value)
             FROM credit_other_concepts_detail cod
             INNER JOIN credit_other_concepts coc ON coc.id = cod.credit_other_concepts_id
             INNER JOIN glotypes g ON g.id = cod.concept_id
             WHERE coc.credit_id    = a.credit_id
               AND coc.quota_number = a.quota_number
               AND g.code = 'IMT'
               AND cod.deleted_at IS NULL
         ), 0)                                                       AS interestMora,
         CASE
                         WHEN a.paid_full = 'N'
                          AND a.expiration_date < CURDATE()
                         THEN DATEDIFF(CURDATE(), a.expiration_date)
                         ELSE 0
                     END AS diasMora
     FROM credit_amortization a
     JOIN credit c               ON c.id  = a.credit_id
     JOIN credit_intention ci    ON ci.id = c.credit_intention_id
     JOIN credit_line cl         ON cl.id = ci.credit_line_id
     JOIN period p               ON p.id  = c.period_id
     JOIN zona z                 ON z.id  = ci.zone_id AND z.id IN (:zonas)
     JOIN person_zona pz_asesor  ON pz_asesor.zona_id = z.id AND pz_asesor.orden = 0
     JOIN user u                 ON u.person_id = pz_asesor.person_id
     JOIN person_zona pz_cliente ON pz_cliente.zona_id   = z.id
                                 AND pz_cliente.person_id = c.person_id
                                 AND pz_cliente.orden > 0
     LEFT JOIN collection_visit cv
                                 ON cv.cuota_id = a.id
                                AND cv.visit_date = :fecha
     WHERE (
         a.id IN (
             SELECT cv2.cuota_id
             FROM collection_visit cv2
             WHERE cv2.credit_id = c.id
               AND cv2.visit_date = :fecha
         )
         OR
         (
             a.quota_number = (
                 SELECT MIN(a2.quota_number)
                 FROM credit_amortization a2
                 WHERE a2.credit_id = a.credit_id
                   AND a2.paid_full = 'N'
             )
             AND NOT EXISTS (
                 SELECT 1
                 FROM collection_visit cv3
                 WHERE cv3.credit_id = c.id
                   AND cv3.visit_date = :fecha
             )
         )
     )
     ORDER BY z.value, pz_cliente.orden, a.expiration_date, a.quota_number
""", nativeQuery = true)
    List<DailyCollectionProjection> findDailyCollection(
            @Param("zonas") List<Long> zonas,
            @Param("fecha") LocalDate fecha
    );

    @Query(value = """
    SELECT
        nr.credit_id                                                AS creditId,
        ROW_NUMBER() OVER (
            PARTITION BY nr.credit_id
            ORDER BY MIN(nr.created_at)
        )                                                           AS grupoId,
        MIN(nr.created_at)                                          AS fechaInicio,
        MAX(nr.created_at)                                          AS fechaFin,
        COALESCE(SUM(nr.value_paid), 0)                            AS totalPagado,
        DAYNAME(MIN(nr.created_at))                                AS nombreDia
    FROM new_recaudo nr
    WHERE nr.credit_id IN (:creditIds)
    GROUP BY nr.credit_id, DATE(nr.created_at)
    ORDER BY nr.credit_id, MIN(nr.created_at)
""", nativeQuery = true)
    List<DailyCollectionRespaldoProjection> finDailyCollectionRespaldo(
            @Param("creditIds") List<Long> creditIds
    );

}
