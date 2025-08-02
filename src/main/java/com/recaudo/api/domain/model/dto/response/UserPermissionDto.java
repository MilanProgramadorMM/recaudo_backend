package com.recaudo.api.domain.model.dto.response;

import lombok.*;

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
    private Boolean permiso;
}
