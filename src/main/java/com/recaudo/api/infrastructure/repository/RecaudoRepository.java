package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseProjection;
import com.recaudo.api.domain.model.dto.response.RecaudoResponseProjection;
import com.recaudo.api.domain.model.entity.RecaudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RecaudoRepository extends JpaRepository<RecaudoEntity, Long> {

    /**
     * Obtiene todos los recaudos de un crédito
     */
    List<RecaudoEntity> findByCreditId(Long creditId);

    /**
     * Obtiene todos los recaudos de una cuota específica
     */
    List<RecaudoEntity> findByCuotaId(Long cuotaId);

    @Query("""
    SELECT r
    FROM RecaudoEntity r
    JOIN ConceptEntity c ON r.conceptId = c.id
    WHERE r.creditId = :creditId
    ORDER BY r.id DESC
""")
    List<RecaudoEntity> findRecaudosRRByCreditId(@Param("creditId") Long creditId);

    @Query(value = """
        SELECT
            r.id                                   AS recaudoId,
            ci.fullname                            AS clientName,
            r.value_paid                           AS valuePaid,
            r.investment_value                     AS investmentValue,
            r.interest_value                       AS interestValue,
            r.life_insurance                       AS lifeInsurance,
            r.portfolio_insurance                  AS portfolioInsurance,
            r.user_create                          AS userCreate,
            z.value                                AS zona,
            DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt,
            'Pago de Cuota' AS description
        FROM closing cl
        INNER JOIN person p
            ON p.id = cl.person_id
        INNER JOIN user u
            ON u.person_id = p.id
        INNER JOIN recaudo r
            ON r.user_create COLLATE utf8mb4_general_ci = u.username
        INNER JOIN credit c
            ON c.id = r.credit_id
        INNER JOIN credit_intention ci
            ON ci.id = c.credit_intention_id
        INNER JOIN zona z
            ON z.id = ci.zone_id
        WHERE cl.id = :closingId
          AND DATE(r.created_at) = :fecha
          AND ci.zone_id = :zonaId
          AND r.deleted_at IS NULL
        
        UNION ALL
        
        SELECT
        	1 AS recaudoId,
            p.fullname AS clientName,
            c.initial_value_payment AS valuePaid,
            0 AS investmentValue,
            0 AS interestValue,
            0 AS lifeInsurance,
            0 AS portfolioInsurance,
            c.user_create AS userCreate,
            z.value AS zona,
            DATE_FORMAT(c.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt,
            'Cuota Inicial' AS description
        FROM credit c
        INNER JOIN person p ON c.person_id = p.id
        INNER JOIN credit_intention ci ON c.credit_intention_id = ci.id
        INNER JOIN zona z ON ci.zone_id = z.id
        WHERE 1=1
        AND DATE(c.created_at) = :fecha
        AND c.initial_value_payment > 0
        AND z.id = :zonaId;
    """, nativeQuery = true)
    List<RecaudoResponseProjection> findRecaudosWithClientName(
            @Param("closingId") Long closingId,
            @Param("fecha") LocalDate fecha,
            @Param("zonaId") Long zonaId
    );

    @Query(value = """
        SELECT COALESCE(SUM(value_paid), 0)
        FROM recaudo
        WHERE cuota_id = :cuotaId
        """, nativeQuery = true)
    BigDecimal getTotalByCuotaId(@Param("cuotaId") Long cuotaId);

    @Query(value = "SELECT COALESCE(SUM(portfolio_insurance), 0) FROM recaudo WHERE cuota_id = :cuotaId", nativeQuery = true)
    BigDecimal getPortfolioInsuranceByCuotaId(@Param("cuotaId") Long cuotaId);

    @Query(value = "SELECT COALESCE(SUM(life_insurance), 0) FROM recaudo WHERE cuota_id = :cuotaId", nativeQuery = true)
    BigDecimal getLifeInsuranceByCuotaId(@Param("cuotaId") Long cuotaId);

    @Query(value = "SELECT COALESCE(SUM(interest_value), 0) FROM recaudo WHERE cuota_id = :cuotaId", nativeQuery = true)
    BigDecimal getInterestByCuotaId(@Param("cuotaId") Long cuotaId);

    @Query(value = "SELECT COALESCE(SUM(investment_value), 0) FROM recaudo WHERE cuota_id = :cuotaId", nativeQuery = true)
    BigDecimal getInvestmentByCuotaId(@Param("cuotaId") Long cuotaId);

    @Query(value = """
    SELECT
        ci.id                                      AS intentionId,
        ci.fullname                                AS clientName,
        ci.document                                AS document,
        ci.total_capital_value                     AS totalCapitalValue,
        ci.total_intention_value                   AS totalIntentionValue,
        ci.quota_value                             AS quotaValue,
        ci.period_quantity                         AS periodQuantity,
        z.value                                    AS zona,
        DATE_FORMAT(ci.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt
    FROM closing cl
    INNER JOIN person p
        ON p.id = cl.person_id
    INNER JOIN user u
        ON u.person_id = p.id
    INNER JOIN credit_intention ci
        ON ci.user_create COLLATE utf8mb4_general_ci = u.username
    INNER JOIN zona z
        ON z.id = ci.zone_id
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
