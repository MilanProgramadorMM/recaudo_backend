package com.davivienda.kata.infrastructure.repository;

import com.davivienda.kata.domain.model.entity.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

    List<SurveyEntity> findByUserId(Long userId);

}
