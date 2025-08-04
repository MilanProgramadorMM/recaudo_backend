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

    @Query(value = """
            SELECT
                json_agg(main_level)
              FROM (
                SELECT json_build_object(
                    'key', unaccent(lower(m.name)),
                    'label', m.name,
                    'isTitle', CASE m."type"
                                WHEN 1 THEN TRUE
                                ELSE false
                            END,
                    'children', (
                        SELECT json_agg(child_level2)
                        FROM (
                            SELECT json_build_object(
                                'key', unaccent(lower(m2.name)),
                                'label', m2.name,     
                                'icon', m2.icon,
                                'url', m2.url,
                                'children', (
                                    SELECT json_agg(child_level3)
                                    FROM (
                                        SELECT json_build_object(
                                            'key', unaccent(lower(m3.name)),
                                            'label', m3.name,
                                            'url', m3.url,                   
                                            'parentKey', unaccent(lower(m2.name))
                                        ) AS child_level3
                                        FROM module m3
                                        WHERE m3.parent = m2.id
                                            AND m3."type" = 3
                                            AND EXISTS (
                                                SELECT 1
                                                FROM user_permission up3
                                                JOIN action a3 ON a3.id = up3.action_id
                                                WHERE up3.user_id = up.user_id
                                                    AND up3.module_id = m3.id
                                                    AND a3.name = 'VER'
                                            )
                                        ORDER BY m3."order"
                                    ) AS ordered_level3
                                )
                            ) AS child_level2
                            FROM module m2
                            WHERE m2.parent = m.id
                                AND m2."type" = 2
                                AND EXISTS (
                                    SELECT 1
                                    FROM user_permission up2
                                    JOIN action a2 ON a2.id = up2.action_id
                                    WHERE up2.user_id = up.user_id
                                        AND up2.module_id = m2.id
                                        AND a2.name = 'VER'
                                )
                            ORDER BY m2."order"
                        ) AS ordered_level2
                    )
                ) AS main_level
                FROM user_permission up
                INNER JOIN module m ON up.module_id = m.id
                WHERE up.user_id = :userId
                  AND m."type" = 1
                ORDER BY m."order"
              ) AS ordered_main
        """, nativeQuery = true)
    String findEnabledPermissionsByUserId(@Param("userId") Long userId);
}
