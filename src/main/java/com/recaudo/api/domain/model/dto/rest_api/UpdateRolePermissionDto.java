package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateRolePermissionDto {
    private Integer id;
    private Integer rolId;
    private Integer moduleId;
    private Integer actionId;
    private Boolean permiso;
}
