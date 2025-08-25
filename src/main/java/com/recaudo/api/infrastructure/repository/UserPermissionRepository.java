package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.UserPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, Long> {

    /*@Query("""
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

     */

    Optional<UserPermissionEntity> findByUserIdAndModuleIdAndActionId(Integer userId, Integer moduleId, Integer actionId);


    /*@Query(value = """
        WITH allowed_pairs AS (
            SELECT module_id, action_id
            FROM ModuleActionEntity
        ),
        user_roles AS (
            SELECT r.id, r.name
            FROM RoleEntity r
            JOIN UserRoleEntity ur ON ur.roleId = r.id
            WHERE ur.userId = :userId
        ),
        roles_or_none AS (
            SELECT id, name FROM user_roles
            UNION ALL
            SELECT NULL::int AS id, NULL::text AS name
            WHERE NOT EXISTS (SELECT 1 FROM user_roles)
        )
        SELECT DISTINCT ON (up.id, mo.id)
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
        JOIN ModuleEntity mo ON mo.id = ap.module_id
        JOIN ActionEntity ac ON ac.id = ap.action_id
        LEFT JOIN UserPermissionEntity up
            ON up.moduleId = ap.module_id
            AND up.actionId = ap.action_id
            AND up.userId = :userId
        LEFT JOIN RolePermissionEntity rp
            ON rp.moduleId = ap.module_id
            AND rp.actionId = ap.action_id
            AND rp.roleId = r.id
        ORDER BY mo.name, ac.name
    """, nativeQuery = true)
    List<UserPermissionDto> findPermissionsByUserId(@Param("userId") Long userId);*/

    @Query(value = """
        WITH all_permissions AS (
              SELECT
                  up.module_id,
                  up.action_id,
                  up.allow,
                  1 AS priority
              FROM user_permission up
              WHERE up.user_id = 1
          
              UNION ALL
          
              SELECT
                  rp.module_id,
                  rp.action_id,
                  rp.allow,
                  2 AS priority
              FROM user_role ur
              JOIN role_permission rp ON rp.role_id = ur.role_id
              WHERE ur.user_id = 1
          ),
          ranked_permissions AS (
              SELECT
                  module_id,
                  action_id,
                  allow,
                  ROW_NUMBER() OVER (
                      PARTITION BY module_id, action_id
                      ORDER BY priority
                  ) AS rn
              FROM all_permissions
          ),
          selected_permissions AS (
              SELECT
                  module_id,
                  action_id,
                  allow
              FROM ranked_permissions
              WHERE rn = 1
          )
          SELECT
              JSON_ARRAYAGG(main_level ORDER BY main_order) AS menu
          FROM (
              SELECT
                  JSON_OBJECT(
                      'key', LOWER(m.name),
                      'order', m.order,
                      'label', m.name,
                      'isTitle', IF(m.type = 1, TRUE, FALSE),
                      'children', (
                          SELECT JSON_ARRAYAGG(
                              JSON_OBJECT(
                                  'key', LOWER(m2.name),
                                  'label', m2.name,
                                  'icon', m2.icon,
                                  'url', m2.url,
                                  'children', (
                                      SELECT JSON_ARRAYAGG(
                                          JSON_OBJECT(
                                              'key', LOWER(m3.name),
                                              'label', m3.name,
                                              'url', m3.url,
                                              'parentKey', LOWER(m2.name)
                                          ) ORDER BY m3.`order`
                                      )
                                      FROM module m3
                                      WHERE m3.parent = m2.id
                                        AND m3.type = 3
                                        AND EXISTS (
                                            SELECT 1
                                            FROM selected_permissions sp3
                                            JOIN action a3 ON a3.id = sp3.action_id
                                            WHERE sp3.module_id = m3.id
                                              AND a3.name = 'VER'
                                              AND sp3.allow = 1
                                        )
                                  )
                              ) ORDER BY m2.`order`
                          )
                          FROM module m2
                          WHERE m2.parent = m.id
                            AND m2.type = 2
                            AND EXISTS (
                                SELECT 1
                                FROM selected_permissions sp2
                                JOIN action a2 ON a2.id = sp2.action_id
                                WHERE sp2.module_id = m2.id
                                  AND a2.name = 'VER'
                                  AND sp2.allow = 1
                            )
                      )
                  ) AS main_level,
                  m.`order` AS main_order
              FROM module m
              WHERE m.type = 1
                AND EXISTS (
                    SELECT 1
                    FROM selected_permissions sp
                    JOIN action a ON a.id = sp.action_id
                    WHERE sp.module_id = m.id
                      AND a.name = 'VER'
                      AND sp.allow = 1
                )
          ) AS ordered_main;
                              
        """, nativeQuery = true)
    String findEnabledPermissionsByUserId(@Param("userId") Long userId);


}
