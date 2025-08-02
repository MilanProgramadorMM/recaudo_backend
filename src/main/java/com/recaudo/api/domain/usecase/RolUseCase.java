package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.RolGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;
import com.recaudo.api.domain.model.entity.UserEntity;
import com.recaudo.api.domain.model.entity.UserRoleEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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

    public void assignRole(UserRoleUpdateDto dto) {
        rolGateway.assignRole(dto);
    }

}
