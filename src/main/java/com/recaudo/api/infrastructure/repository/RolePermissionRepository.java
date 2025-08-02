package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
