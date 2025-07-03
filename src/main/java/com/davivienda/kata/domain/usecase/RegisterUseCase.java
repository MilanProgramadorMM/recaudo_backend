package com.davivienda.kata.domain.usecase;

import com.davivienda.kata.config.UseCase;
import com.davivienda.kata.domain.gateway.UserGateway;
import com.davivienda.kata.domain.model.dto.response.DefaultResponseDto;
import com.davivienda.kata.domain.model.dto.response.LoginResponseDto;
import com.davivienda.kata.domain.model.dto.rest_api.LoginDto;
import com.davivienda.kata.domain.model.dto.rest_api.RegisterDto;
import com.davivienda.kata.domain.model.entity.UserEntity;
import com.davivienda.kata.exception.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@UseCase
@AllArgsConstructor
public class RegisterUseCase {

    private final static String ERROR_MESSAGE = "Credenciales incorrectos";

    private UserGateway userGateway;
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<DefaultResponseDto<?>> register(RegisterDto userData) throws Exception {

        if (!userData.getPassword1().equals(userData.getPassword2())) {
            throw new BadRequestException("Las contraseñas no son iguales");
        }

        if (userGateway.getByUser(userData.getUsername()) != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(DefaultResponseDto.builder()
                            .message("El usuario ya existe")
                            .status(HttpStatus.CONFLICT)
                            .details("El usuario ya existe.")
                            .data(UserEntity.builder().build())
                            .build());
        }

        String hashedPassword = passwordEncoder.encode(userData.getPassword1());
        UserEntity newUser = UserEntity.builder().user(userData.getUsername()).password(hashedPassword).build();
        userGateway.save(newUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DefaultResponseDto.builder()
                        .message("Usuario registrado exitosamente")
                        .status(HttpStatus.CREATED)
                        .details("Usuario registrado exitosamente")
                        .data(newUser)
                        .build());
    }
}
