package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionAmortizationDetailEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreditIntentionAmortizationDetailRepository extends JpaRepository<CreditIntentionAmortizationDetailEntity, Long> {
    List<CreditIntentionAmortizationDetailEntity> findByAmortizationId(Long amortizationId);

    // suma de value agrupada por concepto para todos los maestros de una intención
    @Query(value = """
    SELECT
        d.concept_id,
        SUM(d.value) AS total_value
    FROM credit_intention_amortization_detail d
    WHERE d.amortization_id IN (
        SELECT m.id
        FROM credit_intention_amortizationn m
        WHERE m.credit_intention_id = :creditIntentionId
    )
    GROUP BY d.concept_id
""", nativeQuery = true)
    List<Object[]> sumValueByConcept(@Param("creditIntentionId") Long creditIntentionId);

    // borrar todos los detalles de una intención (en cascada al borrar maestros)
    @Modifying
    @Query(value = """
    DELETE d
    FROM credit_intention_amortization_detail d
    INNER JOIN credit_intention_amortizationn m
        ON m.id = d.amortization_id
    WHERE m.credit_intention_id = :creditIntentionId
""", nativeQuery = true)
    void deleteByCreditIntentionId(@Param("creditIntentionId") Long creditIntentionId);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE cad
        FROM credit_intention_amortization_detail cad
        INNER JOIN credit_intention_amortizationn ca
            ON ca.id = cad.amortization_id
        WHERE ca.credit_intention_id = :creditIntentionId
        """, nativeQuery = true)
    void deleteDetailsByCreditIntentionId(
            @Param("creditIntentionId") Long creditIntentionId
    );
}