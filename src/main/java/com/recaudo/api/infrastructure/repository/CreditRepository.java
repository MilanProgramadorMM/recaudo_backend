package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.CreditFullResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditProjection;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.entity.CreditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditRepository extends JpaRepository<CreditEntity, Long> {

    @Query("""
        SELECT new com.recaudo.api.domain.model.dto.response.CreditFullResponseDto(
            c.id,
            c.creditIntentionId,
            c.quotaValue,
            c.periodQuantity,
            c.totalIntentionValue,
            c.totalInterestValue,
            c.totalCapitalValue,
            c.totalFinancedValue,

            ci.zoneId,
            z.value,
            ci.document,
            ci.fullname,
            ci.phoneNumber,

            ci.creditLineId,
            cl.name
        )
        FROM CreditEntity c
        JOIN CreditIntentionEntity ci ON ci.id = c.creditIntentionId
        LEFT JOIN ZonaEntity z ON z.id = ci.zoneId
        LEFT JOIN CreditLineEntity cl ON cl.id = ci.creditLineId
        WHERE c.deletedAt IS NULL
        ORDER BY c.createdAt DESC
    """)
    List<CreditFullResponseDto> findAllCreditsFull();

    @Query("""
    SELECT new com.recaudo.api.domain.model.dto.response.CreditFullResponseDto(
        c.id,
        c.creditIntentionId,
        c.quotaValue,
        c.periodQuantity,
        c.totalIntentionValue,
        c.totalInterestValue,
        c.totalCapitalValue,
        c.totalFinancedValue,
        ci.zoneId,
        z.value,
        ci.document,
        ci.fullname,
        ci.phoneNumber,
        ci.creditLineId,
        cl.name
    )
    FROM CreditEntity c
    JOIN CreditIntentionEntity ci ON ci.id = c.creditIntentionId
    LEFT JOIN ZonaEntity z ON z.id = ci.zoneId
    LEFT JOIN CreditLineEntity cl ON cl.id = ci.creditLineId
    WHERE c.deletedAt IS NULL AND ci.userCreate = :username
    ORDER BY c.createdAt DESC
""")
    List<CreditFullResponseDto> findCreditsByUsername(@Param("username") String username);

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
                    cl.id AS creditLineId
                FROM credit c
                INNER JOIN person p ON c.person_id = p.id
                INNER JOIN person_zona pz ON p.id = pz.person_id
                INNER JOIN zona z ON pz.zona_id = z.id
                INNER JOIN credit_line cl ON c.credit_line_id = cl.id
                WHERE c.person_id = :personId
                  AND c.deleted_at IS NULL
                ORDER BY c.created_at DESC
                LIMIT 1;
    """, nativeQuery = true)
    Optional<CreditProjection> findCreditDetailsByPersonId(@Param("personId") Long personId);

    boolean existsByCreditIntentionId(Long creditIntentionId);

    /**
     * Busca créditos por ID de persona
     */
    Optional<CreditEntity> findByPersonId(Long personId);
    Optional<CreditEntity> findByCreditIntentionId(Long creditIntentionId);
}