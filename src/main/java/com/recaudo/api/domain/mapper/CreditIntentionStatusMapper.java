package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import com.recaudo.api.domain.model.entity.CreditIntentionStatusEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditIntentionStatusMapper {

    CreditIntentionStatusResponseDto entityToDto(CreditIntentionStatusEntity entity);
    CreditIntentionStatusEntity dtoToEntity(ChangeCreditStatusDto entity);
    List<CreditIntentionStatusResponseDto> entitiesToDto(List<CreditIntentionStatusEntity> entity);
    List<CreditIntentionStatusEntity> dtoToEntities(List<CreditIntentionStatusResponseDto> entity);

}
