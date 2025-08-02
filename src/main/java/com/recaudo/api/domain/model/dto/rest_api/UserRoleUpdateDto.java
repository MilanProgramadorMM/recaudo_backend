package com.recaudo.api.domain.model.dto.rest_api;

import lombok.Data;

@Data
public class UserRoleUpdateDto {
    private Long userId;
    private Long roleId;
}
