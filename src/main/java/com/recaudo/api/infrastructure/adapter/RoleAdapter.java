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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    /**
     * Asigna (sincroniza) los roles del usuario con la lista recibida.
     * Añade roles nuevos y elimina los ya existentes que no estén en roleIds.
     */

    @Transactional
    @Override
    public void assignRole(UserRoleUpdateDto dto) {
        Long userId = dto.getUserId();
        List<Long> desiredRoleIds = Optional.ofNullable(dto.getRoleIds()).orElse(Collections.emptyList());

        // Validar existencia de usuario
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new BadRequestException("El usuario con ID " + userId + " no existe.");
        }

        // Validar existencia de roles
        List<Long> notFoundRoles = desiredRoleIds.stream()
                .filter(roleId -> !roleRepository.existsById(roleId))
                .collect(Collectors.toList());
        if (!notFoundRoles.isEmpty()) {
            throw new BadRequestException("Los siguientes roles no existen: " + notFoundRoles);
        }

        // Obtener roles actuales del usuario
        List<UserRoleEntity> currentUserRoles = userRoleRepository.findAllByUserId(userId);
        Set<Long> currentRoleIds = currentUserRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toSet());

        Set<Long> desiredSet = new HashSet<>(desiredRoleIds);

        // Roles a eliminar: están actuales pero no deseados
        Set<Long> rolesToRemove = new HashSet<>(currentRoleIds);
        rolesToRemove.removeAll(desiredSet);

        // Roles a agregar: están deseados pero no actuales
        Set<Long> rolesToAdd = new HashSet<>(desiredSet);
        rolesToAdd.removeAll(currentRoleIds);

        // Eliminar los que sobran
        if (!rolesToRemove.isEmpty()) {
            List<UserRoleEntity> toDelete = currentUserRoles.stream()
                    .filter(ur -> rolesToRemove.contains(ur.getRoleId()))
                    .collect(Collectors.toList());
            userRoleRepository.deleteAll(toDelete);
        }
        // Agregar los nuevos
        for (Long roleId : rolesToAdd) {
            UserRoleEntity newUr = UserRoleEntity.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRoleRepository.save(newUr);
        }
    }

    private RoleDto mapToDto(RoleEntity entity) {
        return RoleDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
