package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.rest_api.UserDto;
import com.recaudo.api.domain.model.entity.PersonEntity;
import com.recaudo.api.domain.model.entity.UserEntity;

import java.util.List;

public interface UserGateway {

    public UserDto getById(Long id);
    List<UserDto> getAll();
    public UserEntity saveUserToPerson(PersonEntity personEntity);
}
