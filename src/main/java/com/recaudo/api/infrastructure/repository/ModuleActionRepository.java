package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.domain.model.entity.ModuleActionEntity;
import com.recaudo.api.domain.model.entity.UserPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleActionRepository extends JpaRepository<ModuleActionEntity, Long> {
    @Query(value = """
    WITH allowed_pairs AS (
        SELECT module_id, action_id
        FROM module_action
    ),
    user_roles AS (
        SELECT r.id, r.name
        FROM role r
        JOIN user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = :userId
    ),
    roles_or_none AS (
        SELECT id, name FROM user_roles
        UNION ALL
        SELECT NULL::int AS id, NULL::text AS name
        WHERE NOT EXISTS (SELECT 1 FROM user_roles)
    )
    SELECT DISTINCT ON (ac.id, mo.id)
        COALESCE(up.id, rp.id) AS id,
        mo.id AS moduleId,
        mo.name AS modulo,
        ac.id AS actionId,
        ac.name AS accion,
        CASE
            WHEN up.id IS NOT NULL THEN up.allow
            WHEN rp.id IS NOT NULL THEN rp.allow
            ELSE FALSE
        END AS permiso
    FROM roles_or_none r
    CROSS JOIN allowed_pairs ap
    JOIN module mo ON mo.id = ap.module_id
    JOIN action ac ON ac.id = ap.action_id
    LEFT JOIN user_permission up
        ON up.module_id = ap.module_id
        AND up.action_id = ap.action_id
        AND up.user_id = :userId
    LEFT JOIN role_permission rp
        ON rp.module_id = ap.module_id
        AND rp.action_id = ap.action_id
        AND rp.role_id = r.id
    ORDER BY ac.id, mo.id, mo.name, ac.name
""", nativeQuery = true)
    List<UserPermissionDto> findPermissionsByUserId(@Param("userId") Long userId);



}
