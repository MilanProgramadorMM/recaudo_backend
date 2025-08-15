package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto entityToDto(UserEntity entity);
    UserEntity dtoToEntity(UserDto entity);
    List<UserDto> entitiesToDto(List<UserEntity> entity);
    List<UserEntity> dtoToEntities(List<UserDto> entity);
}
