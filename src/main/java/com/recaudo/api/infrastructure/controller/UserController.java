package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPasswordDto;
import com.recaudo.api.domain.model.dto.rest_api.UserCreateDto;
import com.recaudo.api.domain.usecase.RegisterUseCase;
import com.recaudo.api.exception.BadRequestException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "Register persons")
@AllArgsConstructor
public class UserController {

    private final RegisterUseCase userUseCase;

    @GetMapping("/get/{id}")
    public ResponseEntity<DefaultResponseDto<UserDto>> getByUserId(@PathVariable("id") Long id) {

        UserDto data = userUseCase.getById(id);

        return ResponseEntity.ok(
            DefaultResponseDto.<UserDto>builder()
                .message("Información encontrada")
                .status(HttpStatus.OK)
                .details("Información encontrada")
                .data(data)
                .build()
        );
    }

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<UserDto>>builder()
                        .message("Personas encontradas")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(users)
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<DefaultResponseDto<UserDto>> registerUser(
            @RequestBody @Valid UserCreateDto data,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
            DefaultResponseDto.<UserDto>builder()
                .message("Usuario creado correctamente")
                .status(HttpStatus.OK)
                .details("Los datos fueron creados")
                .data(userUseCase.register(data))
                .build());
    }

    @PostMapping("/update-username")
    public ResponseEntity<DefaultResponseDto<UserDto>> updateUsername(
            @Valid @RequestBody UpdateUserDto userDto) {

        userUseCase.updateUsername(userDto);

        return ResponseEntity.ok(
                DefaultResponseDto.<UserDto>builder()
                        .message("Username actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Username cambiado a: " + userDto.getValue())
                        .build()
        );
    }

    @PostMapping("/update-password")
    public ResponseEntity<DefaultResponseDto<UserDto>> updatePassword(
            @Valid @RequestBody UpdateUserPasswordDto userDto) {

        userUseCase.updatePassword(userDto);

        return ResponseEntity.ok(
                DefaultResponseDto.<UserDto>builder()
                        .message("Contraseña actualizada correctamente")
                        .status(HttpStatus.OK)
                        .details("La contraseña fue cambiada")
                        .build()
        );
    }

    @PutMapping("/delete/{userId}")
    public ResponseEntity<DefaultResponseDto<UserDto>> updateUsername(
            @PathVariable("userId") Long userId) {

        userUseCase.deleteUser(userId);

        return ResponseEntity.ok(
                DefaultResponseDto.<UserDto>builder()
                        .message("Usuario desactivado correctamente")
                        .status(HttpStatus.OK)
                        .details("Usuario desactivado")
                        .build()
        );
    }

}
