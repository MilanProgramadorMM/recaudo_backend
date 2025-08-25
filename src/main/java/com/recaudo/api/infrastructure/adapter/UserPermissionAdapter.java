package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.domain.model.entity.UserPermissionEntity;
import com.recaudo.api.infrastructure.repository.ModuleActionRepository;
import com.recaudo.api.infrastructure.repository.UserPermissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserPermissionAdapter {

    @Autowired
    private UserPermissionRepository repository;

    @Autowired
    private ModuleActionRepository moduleActionRepository;

    public List<UserPermissionDto> getPermissionsByUser(Long userId) {
        return moduleActionRepository.findPermissionsByUserId(userId)
                .stream()
                .map(p -> UserPermissionDto.builder()
                        .id(p.getId())                // Long
                        .moduleId(p.getModuleId())    // Long
                        .modulo(p.getModulo())
                        .actionId(p.getActionId())    // Long
                        .accion(p.getAccion())
                        .permiso(p.getPermiso())      // Boolean
                        .build())
                .toList();
    }



    public void upsertUserPermission(Integer userId, Integer moduleId, Integer actionId, Boolean allow) {
        Optional<UserPermissionEntity> existingPermission = repository
                .findByUserIdAndModuleIdAndActionId(userId, moduleId, actionId);

        UserPermissionEntity entity = existingPermission
                .map(p -> {
                    p.setAllow(allow);
                    return p;
                })
                .orElseGet(() -> UserPermissionEntity.builder()
                        .userId(userId)
                        .moduleId(moduleId)
                        .actionId(actionId)
                        .allow(allow)
                        .createdAt(LocalDateTime.now())
                        .build());

        repository.save(entity);
    }



}
