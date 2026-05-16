package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditOtherConceptDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditOtherConceptDetailRepository
        extends JpaRepository<CreditOtherConceptDetailEntity, Long> {

    /** Detalles de un registro de credit_other_concepts. */
    List<CreditOtherConceptDetailEntity> findByCreditOtherConceptId(Long creditOtherConceptId);

    @Query("""
        SELECT COUNT(d) > 0
        FROM CreditOtherConceptDetailEntity d
        JOIN CreditOtherConceptsEntity c ON c.id = d.creditOtherConceptId
        WHERE c.creditId    = :creditId
          AND c.quotaNumber = :quotaNumber
          AND d.conceptId   = :conceptId
    """)
    boolean existsByCreditAndQuotaAndConcept(
            @Param("creditId")    Long creditId,
            @Param("quotaNumber") Integer quotaNumber,
            @Param("conceptId")   Long conceptId
    );

    @Query("""
        SELECT COALESCE(SUM(d.value), 0)
        FROM CreditOtherConceptDetailEntity d
        JOIN CreditOtherConceptsEntity c ON c.id = d.creditOtherConceptId
        WHERE c.creditId    = :creditId
          AND c.quotaNumber = :quotaNumber
          AND d.conceptId   = :conceptId
    """)
    BigDecimal sumByCreditAndQuotaAndConcept(
            @Param("creditId")    Long creditId,
            @Param("quotaNumber") Integer quotaNumber,
            @Param("conceptId")   Long conceptId
    );

    @Query("""
        SELECT COUNT(d)
        FROM CreditOtherConceptDetailEntity d
        JOIN CreditOtherConceptsEntity c ON c.id = d.creditOtherConceptId
        WHERE c.creditId    = :creditId
          AND c.quotaNumber = :quotaNumber
          AND d.conceptId   = :conceptId
    """)
    long countByCreditAndQuotaAndConcept(
            @Param("creditId")    Long creditId,
            @Param("quotaNumber") Integer quotaNumber,
            @Param("conceptId")   Long conceptId
    );
}