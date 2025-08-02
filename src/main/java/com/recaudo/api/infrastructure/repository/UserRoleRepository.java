package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.UserRoleEntity;
import com.recaudo.api.domain.model.entity.UserEntity;
import com.recaudo.api.domain.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    UserRoleEntity findByUserId(Long userId);

}
