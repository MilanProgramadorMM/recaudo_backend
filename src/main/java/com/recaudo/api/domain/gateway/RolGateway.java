package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.RoleCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;

import java.util.List;

public interface RolGateway {

    RoleDto getById(Long id);
    List<RoleDto> getAll();
    RoleDto create(RoleCreateDto roleCreateDto);
    RoleDto update(Long id, RoleCreateDto roleCreateDto);
    void assignRole(UserRoleUpdateDto dto);

}
