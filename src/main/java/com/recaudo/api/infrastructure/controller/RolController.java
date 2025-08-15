package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;
import com.recaudo.api.domain.usecase.RolUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rol")
@Tag(name = "Roles", description = "Roless")
@AllArgsConstructor
public class RolController {

    private final RolUseCase rolUseCase;

    @GetMapping("/get/{id}")
    public ResponseEntity<DefaultResponseDto<RoleDto>> getByUserId(@PathVariable("id") Long id) {

        RoleDto data = rolUseCase.getById(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<RoleDto>builder()
                        .message("Información encontrada")
                        .status(HttpStatus.OK)
                        .details("Información encontrada")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<RoleDto>>> getAllPersons() {
        List<RoleDto> users = rolUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<RoleDto>>builder()
                        .message("Personas encontradas")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(users)
                        .build()
        );
    }

    @PutMapping("/{userId}/assign-role")
    public ResponseEntity<DefaultResponseDto<String>> assignRole(@PathVariable Long userId,
                                                                 @RequestBody UserRoleUpdateDto dto) {
        dto.setUserId(userId);
        rolUseCase.assignRole(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<String>builder()
                        .message("Rol asignado correctamente")
                        .status(HttpStatus.OK)
                        .details("El rol fue actualizado")
                        .data("OK")
                        .build()
        );
    }
}

