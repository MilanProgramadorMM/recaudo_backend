package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.entity.CreditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditRepository extends JpaRepository<CreditEntity, Long> {

    @Query(value = """
    SELECT
          c.id                    AS id,
          c.credit_intention_id   AS creditIntentionId,
          c.credit_status         AS creditStatus,
          c.quota_value           AS quotaValue,
          c.period_quantity       AS periodQuantity,
          c.total_intention_value AS totalIntentionValue,
          c.total_interest_value  AS totalInterestValue,
          c.total_capital_value   AS totalCapitalValue,
          c.total_financed_value  AS totalFinancedValue,
          ci.zone_id              AS zoneId,
          z.value                 AS zoneName,
          ci.document             AS document,
          ci.fullname             AS fullname,
          ci.phone_number         AS phoneNumber,
          ci.credit_line_id       AS creditLineId,
          cl.name                 AS creditLineName,
          c.created_at            AS createdAt,
          -- Días de mora: cuota más antigua sin pagar y vencida
          COALESCE((
              SELECT DATEDIFF(CURDATE(), a.expiration_date)
              FROM credit_amortization a
              WHERE a.credit_id = c.id
                AND a.paid_full = 'N'
                AND a.expiration_date < CURDATE()
              ORDER BY a.expiration_date ASC
              LIMIT 1
          ), 0) AS diasMora
      FROM credit c
      JOIN credit_intention ci ON ci.id = c.credit_intention_id
      LEFT JOIN zona z          ON z.id  = ci.zone_id
      LEFT JOIN credit_line cl  ON cl.id = ci.credit_line_id
      WHERE c.deleted_at IS NULL
      ORDER BY c.id DESC
""", nativeQuery = true)
    List<CreditFullView> findAllCreditsFull();

    @Query(value = """
    SELECT
        c.id                    AS id,
        c.credit_intention_id   AS creditIntentionId,
        c.credit_status         AS creditStatus,
        c.quota_value           AS quotaValue,
        c.period_quantity       AS periodQuantity,
        c.total_intention_value AS totalIntentionValue,
        c.total_interest_value  AS totalInterestValue,
        c.total_capital_value   AS totalCapitalValue,
        c.total_financed_value  AS totalFinancedValue,
        ci.zone_id              AS zoneId,
        z.value                 AS zoneName,
        ci.document             AS document,
        ci.fullname             AS fullname,
        ci.phone_number         AS phoneNumber,
        ci.credit_line_id       AS creditLineId,
        cl.name                 AS creditLineName,
        c.created_at            AS createdAt
    FROM credit c
    JOIN credit_intention ci ON ci.id = c.credit_intention_id
    LEFT JOIN zona z          ON z.id  = ci.zone_id
    LEFT JOIN credit_line cl  ON cl.id = ci.credit_line_id
    WHERE c.deleted_at IS NULL
      AND ci.user_create = :username
    ORDER BY c.id DESC
""", nativeQuery = true)
    List<CreditFullView> findCreditsByUsername(@Param("username") String username);

    @Query(value = """
    SELECT
        c.id                    AS id,
        c.credit_intention_id   AS creditIntentionId,
        c.credit_status         AS creditStatus,
        c.quota_value           AS quotaValue,
        c.period_quantity       AS periodQuantity,
        c.total_intention_value AS totalIntentionValue,
        c.total_interest_value  AS totalInterestValue,
        c.total_capital_value   AS totalCapitalValue,
        c.total_financed_value  AS totalFinancedValue,
        ci.zone_id              AS zoneId,
        z.value                 AS zoneName,
        ci.document             AS document,
        ci.fullname             AS fullname,
        ci.phone_number         AS phoneNumber,
        ci.credit_line_id       AS creditLineId,
        cl.name                 AS creditLineName,
        c.created_at            AS createdAt
    FROM credit c
    JOIN credit_intention ci ON ci.id = c.credit_intention_id
    LEFT JOIN zona z          ON z.id  = ci.zone_id
    LEFT JOIN credit_line cl  ON cl.id = ci.credit_line_id
    WHERE c.deleted_at IS NULL
      AND z.id = :zona
    ORDER BY c.id DESC
""", nativeQuery = true)
    List<CreditFullView> findCreditsByZona(@Param("zona") Long zona);

    @Query("""
        SELECT new com.recaudo.api.domain.model.dto.response.CreditResponseDto(
            c.id,
            c.creditIntentionId,
            c.personId,
            c.creditLineId,
            c.quotaValue,
            c.periodId,
            c.periodQuantity,
            c.taxTypeId,
            c.taxValue,
            c.totalIntentionValue,
            c.totalInterestValue,
            c.totalCapitalValue,
            c.itemValue,
            c.initialValuePayment,
            c.totalFinancedValue        )
        FROM CreditEntity c
        WHERE c.id = :id AND c.deletedAt IS NULL
    """)
    CreditResponseDto findBy(@Param("id") Long id);

    @Query(value = """
        SELECT
                                        c.id AS id,
                                        c.credit_intention_id AS creditIntentionId,
                                        c.quota_value AS quotaValue,
                                        c.period_quantity AS periodQuantity,
                                        c.total_intention_value AS totalIntentionValue,
                                        c.total_interest_value AS totalInterestValue,
                                        c.total_capital_value AS totalCapitalValue,
                                        c.total_financed_value AS totalFinancedValue,
                                        cl.id AS creditLineId,
                                        cl.name AS creditLineName,
                                        CONCAT(p.firstname, ' ', p.lastname) AS fullname,
                                        p.document AS document,
                                        z.value AS zoneName,
                                        per.name AS periodName,
                                        c.credit_status
                                    FROM credit c
                                    INNER JOIN person p ON c.person_id = p.id
                                    LEFT JOIN person_zona pz ON p.id = pz.person_id AND pz.is_asesor = false
                                    LEFT JOIN zona z ON pz.zona_id = z.id
                                    LEFT JOIN credit_line cl ON c.credit_line_id = cl.id
                                    LEFT JOIN period per ON c.period_id = per.id
                                    WHERE c.person_id = :personId
                                      AND c.deleted_at IS NULL
                                    ORDER BY c.created_at DESC
    """, nativeQuery = true)
    List<CreditProjection> findCreditDetailsByPersonId(@Param("personId") Long personId);

    boolean existsByCreditIntentionId(Long creditIntentionId);

    /**
     * Busca créditos por ID de persona
     */
    Optional<CreditEntity> findByPersonId(Long personId);
    Optional<CreditEntity> findByCreditIntentionId(Long creditIntentionId);


    @Query(value = """
            SELECT
                c.id                          AS id,
                c.credit_intention_id         AS creditIntentionId,
                c.quota_value                 AS quotaValue,
                c.period_quantity             AS periodQuantity,
                c.total_intention_value       AS totalIntentionValue,
                c.total_interest_value        AS totalInterestValue,
                c.total_capital_value         AS totalCapitalValue,
                c.total_financed_value        AS totalFinancedValue,
                c.created_at                  AS createdAt,
                ci.zone_id                    AS zoneId,
                z.value                       AS zoneName,
                ci.document                   AS document,
                ci.fullname                   AS fullname,
                ci.phone_number               AS phoneNumber,
                ci.credit_line_id             AS creditLineId,
                cl.name                       AS creditLineName
            FROM credit c
            JOIN credit_intention ci
                ON ci.id = c.credit_intention_id
            JOIN zona z
                ON z.id = ci.zone_id
            JOIN person_zona pz_asesor
                ON pz_asesor.zona_id = z.id
                AND pz_asesor.orden = 0
            JOIN user u
                ON u.person_id = pz_asesor.person_id
                AND u.username = :username
            LEFT JOIN credit_line cl
                ON cl.id = ci.credit_line_id
            WHERE c.deleted_at IS NULL
            ORDER BY c.id DESC
        """, nativeQuery = true)
    List<CreditProjection> findActiveCreditsByAsesorUsername(@Param("username") String username);


    //SIRVE PARA OBTENER LOS CREDITOS CAUSADOS EL DIA ACTUAL ASOSIADOS A UN ASESOR
    @Query(value = """
            SELECT
                c.id AS id,
                c.total_capital_value AS totalCapitalValue,
                c.created_at AS createdAt,
                ci.fullname AS clientName
            FROM credit c
            INNER JOIN credit_intention ci ON c.credit_intention_id = ci.id
            WHERE ci.user_create = :asesorUsername
              AND DATE(c.created_at) = :closingDate
              AND c.deleted_at IS NULL
              AND c.credit_line_id IN (:ids)
    """, nativeQuery = true)
    List<CreditCausadoProjection> findCreditsCausadosByAsesorAndDateAndCreditLine(
            @Param("asesorUsername") String asesorUsername,
            @Param("closingDate") LocalDate closingDate,
            @Param("ids") List<Long> ids
    );

    @Query(value = """
        SELECT
            clo.id          AS idCierre,
            p.fullname      AS asesor,
            z.description   AS zona
        FROM credit c
        INNER JOIN credit_intention ci
            ON ci.id = c.credit_intention_id
        INNER JOIN zona z
            ON z.id = ci.zone_id
        INNER JOIN closing clo
            ON clo.zona_id = z.id
           AND clo.person_id = :personId
        INNER JOIN person p
            ON p.id = clo.person_id
        INNER JOIN type_person tp
            ON tp.id = p.type_person_id
        INNER JOIN closing_status cs
            ON cs.closing_id = clo.id
        WHERE c.id = :creditId
          AND tp.id = 1
          AND cs.code = 'PRE_CIERRE'
          AND cs.status = 1
          AND clo.status = 1
        """, nativeQuery = true)
    CierreAsesorView findCierreAsesorAndZona(  @Param("personId") Long personId,
                                        @Param("creditId") Long creditId);

}