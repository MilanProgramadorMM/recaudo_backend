package com.davivienda.kata.infrastructure.adapter;

import com.davivienda.kata.domain.gateway.SurveyGateway;
import com.davivienda.kata.domain.model.entity.AnswerEntity;
import com.davivienda.kata.domain.model.entity.SurveyEntity;
import com.davivienda.kata.infrastructure.repository.AnswerRepository;
import com.davivienda.kata.infrastructure.repository.SurveyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SurveyAdapter implements SurveyGateway {

    private SurveyRepository repository;
    private AnswerRepository answerRepository;

    @Override
    public SurveyEntity getById(Long survey) {
        return repository.findById(survey).orElse(null);
    }

    @Override
    public List<SurveyEntity> getAllByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public SurveyEntity save(SurveyEntity survey) {
        return repository.save(survey);
    }

    @Override
    public AnswerEntity saveAnswer(AnswerEntity answerEntity) {
        return answerRepository.save(answerEntity);
    }

    @Override
    public void delete(Long surveyId) {
        repository.findById(surveyId).ifPresent(
                surveyEntity -> repository.delete(surveyEntity)
        );
    }
}
