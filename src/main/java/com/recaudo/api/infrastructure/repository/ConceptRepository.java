package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ConceptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConceptRepository extends JpaRepository<ConceptEntity, Long> {

    Optional<ConceptEntity> findByConceptKey(String conceptKey);

}
