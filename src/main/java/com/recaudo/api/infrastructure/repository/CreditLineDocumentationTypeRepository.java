package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditLineDocumentationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditLineDocumentationTypeRepository extends JpaRepository<CreditLineDocumentationTypeEntity, Long> {
}
