package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.TypePersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TypePersonRepository extends JpaRepository<TypePersonEntity, Long> {
    Optional<TypePersonEntity> findByValue(String value);
}
