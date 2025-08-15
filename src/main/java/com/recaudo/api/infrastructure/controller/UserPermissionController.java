package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPermissionDto;
import com.recaudo.api.infrastructure.adapter.UserPermissionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/permissions")
public class UserPermissionController {


    @Autowired
    UserPermissionAdapter getPermissionsByUser;

    @GetMapping("/user/{userId}")
    public ResponseEntity<DefaultResponseDto<List<UserPermissionDto>>> getPermissionsByUser(@PathVariable Long userId) {
        List<UserPermissionDto> permissions = getPermissionsByUser.getPermissionsByUser(userId);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<UserPermissionDto>>builder()
                        .message("Permisos obtenidos correctamente")
                        .status(HttpStatus.OK)
                        .details("Permisos asociados al usuario ID: " + userId)
                        .data(permissions)
                        .build()
        );
    }

    @PutMapping("/upsert")
    public ResponseEntity<DefaultResponseDto<String>> upsertUserPermission(@RequestBody UpdateUserPermissionDto dto) {
        try {
            getPermissionsByUser.upsertUserPermission(dto.getUserId(), dto.getModuleId(), dto.getActionId(), dto.getPermiso());
            return ResponseEntity.ok(
                    DefaultResponseDto.<String>builder()
                            .message("Permiso actualizado/creado correctamente")
                            .status(HttpStatus.OK)
                            .details("Permiso actualizado o creado")
                            .data("OK")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DefaultResponseDto.<String>builder()
                            .message("Error al actualizar/crear permiso")
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .details(e.getMessage())
                            .build());
        }
    }




}
