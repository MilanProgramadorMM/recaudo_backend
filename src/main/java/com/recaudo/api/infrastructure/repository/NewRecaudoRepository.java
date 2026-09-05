package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseProjection;
import com.recaudo.api.domain.model.dto.response.RecaudoResponseProjection;
import com.recaudo.api.domain.model.entity.NewRecaudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface NewRecaudoRepository extends JpaRepository<NewRecaudoEntity, Long> {

    List<NewRecaudoEntity> findByCreditId(Long creditId);

    List<NewRecaudoEntity> findByQuotaId(Long quotaId);


    /**
     * Suma de value_paid de todos los recaudos de una cuota.
     * Resultado negativo = pagos realizados.
     */
    @Query(value = """
        SELECT COALESCE(SUM(r.value_paid), 0)
        FROM new_recaudo r
        WHERE r.quota_id = :quotaId
    """, nativeQuery = true)
    BigDecimal getTotalValuePaidByQuotaId(@Param("quotaId") Long quotaId);

    // ── Recaudos en ruta para cierre ─────────────────────────
    @Query(value = """
        SELECT
            r.id                                        AS recaudoId,
            ci.fullname                                 AS clientName,
            r.value_paid                                AS valuePaid,
            u.username                                  AS userCreate,
            z.value                                     AS zona,
            DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt,
            'Pago de Cuota'                             AS description,
            ca.quota_number                             AS numeroCuota,
            fn_contar_cuotas_pendientes(c.id)           AS cuotasPendientes,
            gt.name                                     AS metodoPago
        FROM closing cl
        INNER JOIN person p   ON p.id   = cl.person_id
        INNER JOIN user u     ON u.person_id = p.id
        INNER JOIN new_recaudo r
            ON r.user_create COLLATE utf8mb4_general_ci = u.username
        INNER JOIN credit c   ON c.id   = r.credit_id
        INNER JOIN credit_intention ci ON ci.id = c.credit_intention_id
        INNER JOIN zona z     ON z.id   = ci.zone_id
        LEFT JOIN credit_amortization ca ON ca.id = r.quota_id
        LEFT JOIN glotypes gt ON gt.id = r.payment_type_id AND gt.type = 5
        WHERE cl.id = :closingId
          AND DATE(r.created_at) = :fecha
          AND ci.zone_id = :zonaId
        
        UNION ALL
        
        SELECT
            1                                           AS recaudoId,
            p.fullname                                  AS clientName,
            c.initial_value_payment                     AS valuePaid,
            c.user_create                                AS userCreate,
            z.value                                     AS zona,
            DATE_FORMAT(c.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt,
            'Cuota Inicial'                             AS description,
            0                                            AS numeroCuota,
            fn_contar_cuotas_pendientes(c.id)           AS cuotasPendientes,
            NULL                                         AS metodoPago
        FROM credit c
        INNER JOIN person p   ON c.person_id = p.id
        INNER JOIN credit_intention ci ON c.credit_intention_id = ci.id
        INNER JOIN zona z     ON ci.zone_id = z.id
        WHERE DATE(c.created_at) = :fecha
          AND c.initial_value_payment > 0
          AND z.id = :zonaId
    """, nativeQuery = true)
    List<RecaudoResponseProjection> findRecaudosWithClientName(
            @Param("closingId") Long closingId,
            @Param("fecha") LocalDate fecha,
            @Param("zonaId") Long zonaId
    );

    @Query(value = """
        SELECT
            ci.id                                        AS intentionId,
            ci.fullname                                  AS clientName,
            ci.document                                  AS document,
            ci.total_capital_value                       AS totalCapitalValue,
            ci.total_intention_value                     AS totalIntentionValue,
            ci.quota_value                               AS quotaValue,
            ci.period_quantity                           AS periodQuantity,
            z.value                                      AS zona,
            DATE_FORMAT(ci.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt
        FROM closing cl
        INNER JOIN person p   ON p.id   = cl.person_id
        INNER JOIN user u     ON u.person_id = p.id
        INNER JOIN credit_intention ci
            ON ci.user_create COLLATE utf8mb4_general_ci = u.username
        INNER JOIN zona z     ON z.id   = ci.zone_id
        WHERE cl.id = :closingId
          AND DATE(ci.created_at) = :fecha
          AND ci.zone_id = :zonaId
          AND ci.deleted_at IS NULL
        ORDER BY ci.created_at DESC
    """, nativeQuery = true)
    List<CreditIntentionResponseProjection> findIntentionsByUserAndDate(
            @Param("closingId") Long closingId,
            @Param("fecha") LocalDate fecha,
            @Param("zonaId") Long zonaId
    );

}
