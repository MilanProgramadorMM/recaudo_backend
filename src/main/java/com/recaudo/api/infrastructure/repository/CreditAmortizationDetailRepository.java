package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditAmortizationDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditAmortizationDetailRepository extends JpaRepository<CreditAmortizationDetailEntity, Long> {
    List<CreditAmortizationDetailEntity> findByAmortizationId(Long amortizationId);

    List<CreditAmortizationDetailEntity> findByAmortizationIdIn(List<Long> amortizationIds);

}