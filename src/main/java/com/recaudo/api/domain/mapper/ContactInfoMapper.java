package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.ContactInfoRegisterDto;
import com.recaudo.api.domain.model.entity.ContactInfoEntity;
import com.recaudo.api.domain.model.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactInfoMapper {

    ContactInfoRegisterDto entityToDto(ContactInfoEntity entity);
    ContactInfoEntity dtoToEntity(ContactInfoRegisterDto entity);
    List<ContactInfoRegisterDto> entitiesToDto(List<ContactInfoEntity> entity);
    List<ContactInfoEntity> dtoToEntities(List<ContactInfoRegisterDto> entity);
}
