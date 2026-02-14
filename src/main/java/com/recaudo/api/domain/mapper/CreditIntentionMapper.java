package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditIntentionDto;
import com.recaudo.api.domain.model.entity.CreditIntentionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditIntentionMapper {

    CreditIntentionResponseDto entityToDto(CreditIntentionEntity entity);
    CreditIntentionEntity dtoToEntity(CreditIntentionDto entity);
    List<CreditIntentionResponseDto> entitiesToDto(List<CreditIntentionEntity> entity);
    List<CreditIntentionEntity> dtoToEntities(List<CreditIntentionResponseDto> entity);

}
