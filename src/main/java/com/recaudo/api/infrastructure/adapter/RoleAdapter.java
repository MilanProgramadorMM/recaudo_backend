package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.RolGateway;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.RoleCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;
import com.recaudo.api.domain.model.entity.RoleEntity;
import com.recaudo.api.domain.model.entity.RolePermissionEntity;
import com.recaudo.api.domain.model.entity.UserRoleEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.RolePermissionRepository;
import com.recaudo.api.infrastructure.repository.RoleRepository;
import com.recaudo.api.infrastructure.repository.UserRepository;
import com.recaudo.api.infrastructure.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
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
    RolePermissionRepository rolePermissionRepository;

    @Override
    public RoleDto getById(Long id) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Rol no encontrado con ID: " + id));

        return mapToDto(entity);
    }

    @Override
    public List<RoleDto> getAll() {
        List<RoleEntity> entities = roleRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDto create(RoleCreateDto dto) {
        // Validar nombre único
        if (roleRepository.findByName(dto.getName()) != null) {
            throw new BadRequestException("Ya existe un rol con el nombre: " + dto.getName());
        }

        // Crear rol
        RoleEntity entity = RoleEntity.builder()
                .name(dto.getName().toUpperCase())
                .description(dto.getDescription().toUpperCase())
                .hierarchy(2)
                .createdAt(LocalDateTime.now())
                .build();

        roleRepository.save(entity);

        // Buscar rol ADMIN
        RoleEntity adminRole = roleRepository.findByName("Administrador");
        if (adminRole != null) {
            // Buscar permisos de ADMIN
            List<RolePermissionEntity> adminPermissions =
                    rolePermissionRepository.findAll().stream()
                            .filter(rp -> rp.getRoleId().equals(adminRole.getId()))
                            .toList();

            // Clonar los permisos con allow = false para el nuevo rol
            List<RolePermissionEntity> newPermissions = adminPermissions.stream()
                    .map(rp -> RolePermissionEntity.builder()
                            .roleId(entity.getId())
                            .moduleId(rp.getModuleId())
                            .actionId(rp.getActionId())
                            .allow(false)
                            .createdAt(LocalDateTime.now())
                            .build()
                    ).toList();

            rolePermissionRepository.saveAll(newPermissions);
        }

        return mapToDto(entity);
    }

    @Override
    public RoleDto update(Long id, RoleCreateDto dto) {
        // Buscar rol existente
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Rol no encontrado con ID: " + id));

        // Validar nombre único (excepto si es el mismo rol)
        RoleEntity existing = roleRepository.findByName(dto.getName());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BadRequestException("Ya existe un rol con el nombre: " + dto.getName());
        }

        // Actualizar valores
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        roleRepository.save(entity);

        return mapToDto(entity);
    }



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
