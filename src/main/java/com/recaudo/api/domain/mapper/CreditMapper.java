package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditRegisterDto;
import com.recaudo.api.domain.model.entity.CreditEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditMapper {

    CreditResponseDto entityToDto(CreditEntity entity);
    CreditEntity dtoToEntity(CreditRegisterDto entity);
    List<CreditResponseDto> entitiesToDto(List<CreditEntity> entity);
    List<CreditEntity> dtoToEntities(List<CreditResponseDto> entity);

}
