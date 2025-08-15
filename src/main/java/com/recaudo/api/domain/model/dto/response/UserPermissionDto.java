package com.recaudo.api.domain.model.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserPermissionDto {
    private Integer id;
    private Integer moduleId;
    private String modulo;
    private Integer actionId;
    private String accion;
    private Boolean permiso;
}
