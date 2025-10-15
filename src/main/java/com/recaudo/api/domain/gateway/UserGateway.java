package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPasswordDto;
import com.recaudo.api.domain.model.dto.rest_api.UserCreateDto;
import com.recaudo.api.domain.model.entity.PersonEntity;
import com.recaudo.api.domain.model.entity.UserEntity;

import java.util.List;

public interface UserGateway {

    public UserDto getById(Long id);
    List<UserDto> getAll();
    public UserEntity saveUserToPerson(PersonEntity personEntity);
    public UserDto saveUser(UserCreateDto dto);
    void updateUsername(UpdateUserDto userDto);
    void updatePassword(UpdateUserPasswordDto userDto);
    void deleteUser(Long id);
    void reactivateUserFromPerson(PersonEntity personEntity);
    void inactivateUserByPersonId(Long personId);
    UserDto updateStatus(Long userId, boolean status);





}
