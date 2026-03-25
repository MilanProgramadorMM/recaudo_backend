package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.model.dto.response.PersonResponseDto;
import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPasswordDto;
import com.recaudo.api.domain.model.dto.rest_api.UserCreateDto;
import com.recaudo.api.domain.model.entity.UserEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.ResourceNotFoundException;
import com.recaudo.api.infrastructure.adapter.UserDetailsImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@UseCase
@AllArgsConstructor
public class RegisterUseCase {

    private final static String ERROR_MESSAGE = "Credenciales incorrectos";

    private UserGateway userGateway;
    private PasswordEncoder passwordEncoder;
    private PersonGateway personGateway;

    public UserDto register(UserCreateDto data) {
        return userGateway.saveUser(data);
    }

    public UserDto getById(Long id)  {
        return userGateway.getById(id);
    }

    public List<UserDto> getAll() {
        return userGateway.getAll();
    }

    public void updateUsername(UpdateUserDto userDto) {
        userGateway.updateUsername(userDto);
    }

    public void updatePassword(UpdateUserPasswordDto userDto) {
        Long userId = (userDto.getUserId() != null)
                ? userDto.getUserId()
                : getUserIdToken();

        if(userDto.getCurrentPassword() == null || userDto.getCurrentPassword().isEmpty()){
            UserDto user = getById(userId);
            if(user == null){
                throw new ResourceNotFoundException("No existe usuario");
            }

            try{
                PersonResponseDto person = personGateway.getById(user.getPersonId());
                userDto.setNewPassword(person.getDocument());
            }catch (BadRequestException e){
                userDto.setNewPassword(user.getUsername());
            }

            userDto.setUserId(userId);
            userGateway.updatePassword(userDto);
        }else{
            userDto.setUserId(userId);
            userGateway.updatePassword(userDto);
        }
    }

    public void deleteUser(Long userId) {
        userGateway.deleteUser(userId);
    }

    public UserDto updateUserStatus(Long userId, boolean status) {
        return userGateway.updateStatus(userId, status);
    }

    private Long getUserIdToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getId();
    }

}
