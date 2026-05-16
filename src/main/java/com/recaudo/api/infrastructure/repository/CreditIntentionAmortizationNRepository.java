package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionAmortizationNEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditIntentionAmortizationNRepository extends JpaRepository<CreditIntentionAmortizationNEntity, Long> {
    List<CreditIntentionAmortizationNEntity> findByCreditIntentionIdOrderByQuotaNumber(Long creditId);
    void deleteByCreditIntentionId(Long creditId);

}