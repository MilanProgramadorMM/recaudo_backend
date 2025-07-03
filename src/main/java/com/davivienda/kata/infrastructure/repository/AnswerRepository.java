package com.davivienda.kata.infrastructure.repository;

import com.davivienda.kata.domain.model.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

}
