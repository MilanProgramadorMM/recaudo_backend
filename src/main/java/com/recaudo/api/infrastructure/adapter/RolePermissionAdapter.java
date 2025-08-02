package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.entity.RolePermissionEntity;
import com.recaudo.api.infrastructure.repository.RolePermissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RolePermissionAdapter {

    @Autowired
    private RolePermissionRepository repository;

    public List<RolePermissionDto> getPermissionsByRole(Long roleId) {
        return repository.findPermissionsByRoleId(roleId);
    }

    public void updatePermissionAllow(Long id, Boolean allow) {
        RolePermissionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        entity.setAllow(allow);
        repository.save(entity);
    }

}
