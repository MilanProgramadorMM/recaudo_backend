package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionAmortizationNEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditIntentionAmortizationNRepository extends JpaRepository<CreditIntentionAmortizationNEntity, Long> {
    List<CreditIntentionAmortizationNEntity> findByCreditIntentionIdOrderByQuotaNumber(Long creditId);
    //void deleteByCreditIntentionId(Long creditId);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE
        FROM credit_intention_amortizationn
        WHERE credit_intention_id = :creditIntentionId
        """, nativeQuery = true)
    void deleteByCreditIntentionId(
            @Param("creditIntentionId") Long creditIntentionId
    );

}