package com.davivienda.kata.domain.gateway;

import com.davivienda.kata.domain.model.entity.AnswerEntity;
import com.davivienda.kata.domain.model.entity.SurveyEntity;

import java.util.List;

public interface SurveyGateway {
    public SurveyEntity getById(Long survey);
    public List<SurveyEntity> getAllByUser(Long userId);
    public SurveyEntity save(SurveyEntity user);
    public AnswerEntity saveAnswer(AnswerEntity aswerEntity);
    void delete(Long surveyId);
}
