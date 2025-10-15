package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.CreditLineResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditLineCreateDto;
import com.recaudo.api.domain.model.entity.CreditLineEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditLineMapper {

    CreditLineResponseDto entityToDto(CreditLineEntity entity);
    CreditLineEntity dtoToEntity(CreditLineCreateDto entity);
    List<CreditLineResponseDto> entitiesToDto(List<CreditLineEntity> entity);
    List<CreditLineEntity> dtoToEntities(List<CreditLineResponseDto> entity);

}
