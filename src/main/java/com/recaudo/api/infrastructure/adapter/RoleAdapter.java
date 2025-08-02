package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.RolGateway;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;
import com.recaudo.api.domain.model.entity.RoleEntity;
import com.recaudo.api.domain.model.entity.UserRoleEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.RoleRepository;
import com.recaudo.api.infrastructure.repository.UserRepository;
import com.recaudo.api.infrastructure.repository.UserRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleAdapter implements RolGateway {

    private final RoleRepository roleRepository;
    UserRoleRepository userRoleRepository;
    UserRepository userRepository;

    @Override
    public RoleDto getById(Long id) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Rol no encontrado con ID: " + id));

        return mapToDto(entity);
    }

    @Override
    public List<RoleDto> getAll() {
        List<RoleEntity> entities = roleRepository.findAll();

        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private RoleDto mapToDto(RoleEntity entity) {
        return RoleDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public void assignRole(UserRoleUpdateDto dto) {
        UserRoleEntity userRole = userRoleRepository.findByUserId(dto.getUserId());

        boolean userExists = userRepository.existsById(dto.getUserId());
        if (!userExists) {
            throw new BadRequestException("El usuario con ID " + dto.getUserId() + " no existe.");
        }

        // Verificar si el rol existe
        boolean roleExists = roleRepository.existsById(dto.getRoleId());
        if (!roleExists) {
            throw new BadRequestException("El rol con ID " + dto.getRoleId() + " no existe.");
        }

        if (userRole != null) {
            userRole.setRoleId(dto.getRoleId());
            userRoleRepository.save(userRole);
        } else {
            userRole = UserRoleEntity.builder()
                    .userId(dto.getUserId())
                    .roleId(dto.getRoleId())
                    .createdAt(LocalDateTime.now())
                    .build();
            userRoleRepository.save(userRole);
    }
}
}
