package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.dto.rest_api.UserDto;
import com.recaudo.api.domain.usecase.RegisterUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<DefaultResponseDto<List<UserDto>>> getAllPersons() {
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



}
