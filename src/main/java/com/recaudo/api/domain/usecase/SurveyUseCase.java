package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.SurveyGateway;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreateSurveyDto;
import com.recaudo.api.infrastructure.helper.security.jwt.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UseCase
@AllArgsConstructor
public class SurveyUseCase {

    private JwtUtil jwtService;
    private SurveyGateway surveyGateway;

    /*public ResponseEntity<DefaultResponseDto<?>> get(Long survey) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys")
                        .data(surveyGateway.getById(survey))
                        .build());
    }



    public ResponseEntity<DefaultResponseDto<?>> getAll(Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys")
                        .data(surveyGateway.getAllByUser(userId))
                        .build());
    }

    public ResponseEntity<DefaultResponseDto<?>> save(Long userId, CreateSurveyDto survey) {
        SurveyEntity surveyEntity = SurveyEntity.builder().userId(userId).survey(survey.getSurvey()).build();
        if (survey.getId() != null) {
           surveyEntity.setId(survey.getId());
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DefaultResponseDto.builder()
                        .message("Encuesta creada correctamente")
                        .status(HttpStatus.CREATED)
                        .details("Encuesta creada correctamente")
                        .data(surveyGateway.save(surveyEntity))
                        .build());
    }

    public ResponseEntity<DefaultResponseDto<?>> delete(Long surveyId) {
        surveyGateway.delete(surveyId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DefaultResponseDto.builder()
                        .message("Encuesta eliminada correctamente")
                        .status(HttpStatus.OK)
                        .details("Encuesta eliminada correctamente")
                        .data("Success")
                        .build());
    }

    /*public ResponseEntity<DefaultResponseDto<?>> saveAnswer(CreateSurveyDto survey) {
        AnswerEntity answerEntity = AnswerEntity.builder().surveyId(survey.getId()).data(survey.getSurvey()).build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DefaultResponseDto.builder()
                        .message("Respuesta creada correctamente")
                        .status(HttpStatus.CREATED)
                        .details("Respuesta creada correctamente")
                        .data(surveyGateway.saveAnswer(answerEntity))
                        .build());
    }

     */
}
