package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateRolePermissionDto;
import com.recaudo.api.infrastructure.adapter.RolePermissionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role/permissions")
public class RolePermissionController {

    @Autowired
    RolePermissionAdapter getPermissionsByRoleId;


    @GetMapping("/role/{roleId}")
    public ResponseEntity<DefaultResponseDto<List<RolePermissionDto>>> getPermissionsByRole(@PathVariable Long roleId) {
        List<RolePermissionDto> permissions = getPermissionsByRoleId.getPermissionsByRoleAct(roleId);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<RolePermissionDto>>builder()
                        .message("Permisos obtenidos correctamente")
                        .status(HttpStatus.OK)
                        .details("Permisos asociados al rol ID: " + roleId)
                        .data(permissions)
                        .build()
        );
    }

    /*@PutMapping("/update/{rolePermissionId}")
    public ResponseEntity<DefaultResponseDto<String>> updateRolePermission(
            @PathVariable Long rolePermissionId,
            @RequestBody Boolean allow) {

        try {
            getPermissionsByRoleId.updatePermissionAllow(rolePermissionId, allow);
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


     */
    @PutMapping("/update")
    public ResponseEntity<DefaultResponseDto<String>> updateRolePermission(
            @RequestBody UpdateRolePermissionDto dto) {
        try {
            getPermissionsByRoleId.updateOrCreatePermission(dto);
            return ResponseEntity.ok(
                    DefaultResponseDto.<String>builder()
                            .message("Permiso actualizado o creado correctamente")
                            .status(HttpStatus.OK)
                            .details("La operación fue exitosa")
                            .data("OK")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DefaultResponseDto.<String>builder()
                            .message("Error al actualizar o crear permiso")
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .details(e.getMessage())
                            .build());
        }
    }



}
