package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditIntentionStatusRepository extends JpaRepository<CreditIntentionStatusEntity, Long> {
    Optional<CreditIntentionStatusEntity>
    findTopByCreditIntentionIdOrderByStartDateDesc(Long creditIntentionId);

}
