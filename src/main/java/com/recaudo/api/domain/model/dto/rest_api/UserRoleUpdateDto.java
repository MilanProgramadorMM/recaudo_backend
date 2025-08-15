package com.recaudo.api.domain.model.dto.rest_api;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleUpdateDto {
    private Long userId;
    private List<Long> roleIds;
}
