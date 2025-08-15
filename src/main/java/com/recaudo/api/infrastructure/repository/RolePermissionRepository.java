package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.dto.response.RolePermissionProjection;
import com.recaudo.api.domain.model.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    @Query("""
    SELECT new com.recaudo.api.domain.model.dto.response.RolePermissionDto(
        rp.id,
        mo.id,
        mo.name,
        ac.id,
        ac.name,
        rp.allow
        
    )
    FROM RolePermissionEntity rp
    JOIN ModuleEntity mo ON rp.moduleId = mo.id
    JOIN ActionEntity ac ON rp.actionId = ac.id
    WHERE rp.roleId = :roleId
    ORDER BY mo.name, ac.name
""")
    List<RolePermissionDto> findPermissionsByRoleId(@Param("roleId") Long roleId);


    @Query(value = """
        SELECT 
            m.id           AS module_id,
            m.name         AS module_name,
            a.id           AS action_id,
            a.name         AS action_name,
            COALESCE(rp.allow, FALSE) AS allow
        FROM module m
        INNER JOIN module_action ma ON m.id = ma.module_id
        JOIN action a ON a.id = ma.action_id
        LEFT JOIN role_permission rp 
            ON rp.module_id = ma.module_id 
            AND rp.action_id = ma.action_id
            AND rp.role_id = :roleId
        ORDER BY m.id ASC, a.id ASC
        """, nativeQuery = true)
        List<RolePermissionProjection> findPermissionsByRoleIdACt(@Param("roleId") Long roleId);

    Optional<RolePermissionEntity> findByRoleIdAndModuleIdAndActionId(Long roleId, Long moduleId, Long actionId);
}

