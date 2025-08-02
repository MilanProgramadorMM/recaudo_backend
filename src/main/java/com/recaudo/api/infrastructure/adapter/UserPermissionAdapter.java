package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.domain.model.entity.UserPermissionEntity;
import com.recaudo.api.infrastructure.repository.UserPermissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserPermissionAdapter {

    @Autowired
    private UserPermissionRepository repository;

    public List<UserPermissionDto> getPermissionsByUser(Long userId) {
        return repository.findPermissionsByUserId(userId);
    }


    public void updatePermissionAllow(Long id, Boolean allow) {
        UserPermissionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        entity.setAllow(allow);
        repository.save(entity);
    }


}
