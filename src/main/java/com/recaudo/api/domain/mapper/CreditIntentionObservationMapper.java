package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.CreditIntentionObservationResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditIntentionObservationResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import com.recaudo.api.domain.model.entity.ObservationCreditIntentionEntity;
import com.recaudo.api.domain.model.entity.ObservationCreditIntentionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditIntentionObservationMapper {

    CreditIntentionObservationResponseDto entityToDto(ObservationCreditIntentionEntity entity);
    ObservationCreditIntentionEntity dtoToEntity(ChangeCreditStatusDto entity);
    List<CreditIntentionObservationResponseDto> entitiesToDto(List<ObservationCreditIntentionEntity> entity);
    List<ObservationCreditIntentionEntity> dtoToEntities(List<CreditIntentionObservationResponseDto> entity);

}
