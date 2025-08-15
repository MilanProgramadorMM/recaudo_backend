package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.GlotypesDescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlotypesDescriptionRepository extends JpaRepository<GlotypesDescriptionEntity, Long> {
}
