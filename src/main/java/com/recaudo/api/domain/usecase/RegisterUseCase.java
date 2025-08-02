package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.model.dto.rest_api.UserDto;
import com.recaudo.api.domain.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@UseCase
@AllArgsConstructor
public class RegisterUseCase {

    private final static String ERROR_MESSAGE = "Credenciales incorrectos";

    private UserGateway userGateway;
    private PasswordEncoder passwordEncoder;

    public UserEntity register(UserEntity data) {
        //return userGateway.save(data);
        return null;
    }

    public UserDto getById(Long id)  {
        return userGateway.getById(id);
    }

    public List<UserDto> getAll() {
        return userGateway.getAll();
    }
}
