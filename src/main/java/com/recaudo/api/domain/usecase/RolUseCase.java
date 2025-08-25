package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.RolGateway;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.RoleCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class RolUseCase {

    private RolGateway rolGateway;

    public RoleDto getById(Long id)  {
        return rolGateway.getById(id);
    }

    public List<RoleDto> getAll() {
        return rolGateway.getAll();
    }

    public RoleDto create(RoleCreateDto dto){return  rolGateway.create(dto);}

    public RoleDto update(Long id, RoleCreateDto dto){return  rolGateway.update(id, dto);}

    public void assignRole(UserRoleUpdateDto dto) {
        rolGateway.assignRole(dto);
    }

}
