package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;

import java.util.List;

public interface RolGateway {

    public RoleDto getById(Long id);
    List<RoleDto> getAll();

    void assignRole(UserRoleUpdateDto dto);

}
