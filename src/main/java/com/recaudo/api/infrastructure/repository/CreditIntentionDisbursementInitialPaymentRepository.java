package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionInitialPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditIntentionDisbursementInitialPaymentRepository extends JpaRepository<CreditIntentionInitialPaymentEntity, Long> {

    /**
     * Encuentra todos los pagos iniciales activos de una intención de crédito
     */
    List<CreditIntentionInitialPaymentEntity> findByCreditIntentionIdAndStatusTrue(Long creditIntentionId);

    @Query("SELECT d FROM CreditIntentionInitialPaymentEntity d WHERE d.creditIntentionId = :creditIntentionId AND d.status = true ORDER BY d.createdAt DESC")
    List<CreditIntentionInitialPaymentEntity> findActivePayments(@Param("creditIntentionId") Long creditIntentionId);
}