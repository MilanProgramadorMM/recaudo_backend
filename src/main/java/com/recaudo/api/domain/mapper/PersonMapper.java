package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.PersonEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    PersonRegisterDto entityToDto(PersonEntity entity);
    PersonEntity dtoToEntity(PersonRegisterDto entity);
    List<PersonRegisterDto> entitiesToDto(List<PersonEntity> entity);
    List<PersonEntity> dtoToEntities(List<PersonRegisterDto> entity);
}
