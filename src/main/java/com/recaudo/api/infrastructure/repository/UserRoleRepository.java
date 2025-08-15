package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.UserRoleEntity;
import com.recaudo.api.domain.model.entity.UserEntity;
import com.recaudo.api.domain.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    List<UserRoleEntity> findByUserId(Long userId);
    List<UserRoleEntity> findAllByUserId(Long userId);

}
