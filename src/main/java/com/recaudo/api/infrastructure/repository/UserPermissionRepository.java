package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.domain.model.entity.UserPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, Long> {

    @Query("""
        SELECT new com.recaudo.api.domain.model.dto.response.UserPermissionDto(
            up.id,
            mo.id,
            mo.name,
            ac.id,
            ac.name,
            up.allow
        )
        FROM UserPermissionEntity up
        JOIN ModuleEntity mo ON up.moduleId = mo.id
        JOIN ActionEntity ac ON up.actionId = ac.id
        WHERE up.userId = :userId
        ORDER BY mo.name, ac.name
    """)
    List<UserPermissionDto> findPermissionsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT new com.recaudo.api.domain.model.dto.response.UserPermissionDto(
            up.id,
            mo.id,
            mo.name,
            ac.id,
            ac.name,
            up.allow
        )
        FROM UserPermissionEntity up
        JOIN ModuleEntity mo ON up.moduleId = mo.id
        JOIN ActionEntity ac ON up.actionId = ac.id
        WHERE up.userId = :userId
        ORDER BY mo.name, ac.name
    """)
    List<UserPermissionDto> findEnabledPermissionsByUserId(@Param("userId") Long userId);
}
