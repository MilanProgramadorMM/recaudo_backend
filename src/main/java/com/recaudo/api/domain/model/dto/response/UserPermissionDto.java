package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserPermissionDto {
    private Long id;
    private Long moduleId;
    private String modulo;
    private Long actionId;
    private String accion;
    private Integer permiso;
    }


