package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateUserPermissionDto {
    private Integer id;
    private Integer userId;
    private Integer moduleId;
    private String modulo;
    private Integer actionId;
    private String accion;
    private Boolean permiso;
}
