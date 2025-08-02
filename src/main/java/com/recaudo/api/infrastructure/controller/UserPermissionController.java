package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.infrastructure.adapter.RolePermissionAdapter;
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

    @PutMapping("/update/{userPermissionId}")
    public ResponseEntity<DefaultResponseDto<String>> updateUserPermission(
            @PathVariable Long userPermissionId,
            @RequestBody Boolean allow) {

        try {
            getPermissionsByUser.updatePermissionAllow(userPermissionId, allow);
            return ResponseEntity.ok(
                    DefaultResponseDto.<String>builder()
                            .message("Permiso actualizado correctamente")
                            .status(HttpStatus.OK)
                            .details("El permiso fue actualizado")
                            .data("OK")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DefaultResponseDto.<String>builder()
                            .message("Error al actualizar permiso")
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .details(e.getMessage())
                            .build());
        }
    }



}
