package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.SurveyGateway;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SurveyAdapter implements SurveyGateway {

/*
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

    /*@Override
    public AnswerEntity saveAnswer(AnswerEntity answerEntity) {
        return answerRepository.save(answerEntity);
    }


    @Override
    public void delete(Long surveyId) {
        repository.findById(surveyId).ifPresent(
                surveyEntity -> repository.delete(surveyEntity)
        );
    }
     */
}
