package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionDisbursementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditIntentionDisbursementRepository extends JpaRepository<CreditIntentionDisbursementEntity, Long> {

    /**
     * Encuentra todos los desembolsos activos de una intención de crédito
     */
    List<CreditIntentionDisbursementEntity> findByCreditIntentionIdAndStatusTrue(Long creditIntentionId);

    @Query("SELECT d FROM CreditIntentionDisbursementEntity d WHERE d.creditIntentionId = :creditIntentionId AND d.status = true ORDER BY d.createdAt DESC")
    List<CreditIntentionDisbursementEntity> findActiveDisbursements(@Param("creditIntentionId") Long creditIntentionId);
}