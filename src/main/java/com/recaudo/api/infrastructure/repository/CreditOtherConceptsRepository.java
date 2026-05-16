package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditOtherConceptDetailEntity;
import com.recaudo.api.domain.model.entity.CreditOtherConceptsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditOtherConceptsRepository
        extends JpaRepository<CreditOtherConceptsEntity, Long> {

    List<CreditOtherConceptsEntity> findByCreditId(Long creditId);
    Optional<CreditOtherConceptsEntity> findByCreditIdAndQuotaNumber(
            Long creditId, Integer quotaNumber);
}