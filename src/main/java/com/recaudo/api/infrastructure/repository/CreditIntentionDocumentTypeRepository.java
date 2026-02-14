package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionDocumentEntity;
import com.recaudo.api.domain.model.entity.DocumentTypeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditIntentionDocumentTypeRepository extends JpaRepository<CreditIntentionDocumentEntity, Long> {
    List<CreditIntentionDocumentEntity> findByCreditIntentionIdAndStatusTrue(Long creditIntentionId);

    Optional<CreditIntentionDocumentEntity> findByCreditIntentionIdAndDocumentationTypeIdAndDocumentSideAndStatusTrue(
            Long creditIntentionId,
            Long documentationTypeId,
            String documentSide
    );

    List<CreditIntentionDocumentEntity> findAllByCreditIntentionIdAndDocumentationTypeIdAndStatusTrue(
            Long creditIntentionId,
            Long documentationTypeId
    );

    List<CreditIntentionDocumentEntity> findByCreditIntentionIdAndDocumentSideAndStatusTrue(
            Long intentionId,
            String documentSide
    );

}
