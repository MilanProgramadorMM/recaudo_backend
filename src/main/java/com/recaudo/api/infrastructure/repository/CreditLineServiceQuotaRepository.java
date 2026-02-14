package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditLineServiceQuotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditLineServiceQuotaRepository extends JpaRepository<CreditLineServiceQuotaEntity, Long> {
    List<CreditLineServiceQuotaEntity> findByCreditLineIdAndCapitalizeTrue(Long creditLineId);
}