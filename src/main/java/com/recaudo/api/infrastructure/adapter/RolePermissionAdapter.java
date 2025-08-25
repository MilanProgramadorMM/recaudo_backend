package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateRolePermissionDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPermissionDto;
import com.recaudo.api.domain.model.entity.RolePermissionEntity;
import com.recaudo.api.infrastructure.repository.RolePermissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RolePermissionAdapter {

    @Autowired
    private RolePermissionRepository repository;

    public List<RolePermissionDto> getPermissionsByRole(Long roleId) {
        return repository.findPermissionsByRoleId(roleId);
    }

    public List<RolePermissionDto> getPermissionsByRoleAct(Long roleId) {
        return repository.findPermissionsByRoleIdACt(roleId)
                .stream()
                .map(p -> new RolePermissionDto(
                        null,
                        p.getModuleId(),
                        p.getModuleName(),
                        p.getActionId(),
                        p.getActionName(),
                        p.getAllow() != null && p.getAllow() == 1
                )).toList();
    }

    public void updatePermissionAllow(Long id, Boolean allow) {
        RolePermissionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        entity.setAllow(allow);
        repository.save(entity);
    }

    public void updateOrCreatePermission(UpdateRolePermissionDto dto) {
        Optional<RolePermissionEntity> existingPermission = repository.findByRoleIdAndModuleIdAndActionId(
                dto.getRolId().longValue(),
                dto.getModuleId().longValue(),
                dto.getActionId().longValue()
        );

        if (existingPermission.isPresent()) {
            RolePermissionEntity entity = existingPermission.get();
            entity.setAllow(dto.getPermiso());
            repository.save(entity);
        } else {
            RolePermissionEntity newPermission = RolePermissionEntity.builder()
                    .roleId(dto.getRolId().longValue())
                    .moduleId(dto.getModuleId().longValue())
                    .actionId(dto.getActionId().longValue())
                    .allow(dto.getPermiso())
                    .build();
            repository.save(newPermission);
        }
    }


}
