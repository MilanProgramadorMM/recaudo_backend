package com.recaudo.api.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class DashboardMetricsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BigDecimal getTotalRecaudado(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long zonaId
    ) {

        String sql = """
        SELECT COALESCE(SUM(dr.value), 0)
        FROM dashboard_recaudo dr
        WHERE dr.zona_id = :zonaId
          AND dr.created_at >= :fechaInicio
          AND dr.created_at < :fechaFin
    """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("zonaId", zonaId)
                        .addValue("fechaInicio", fechaInicio)
                        .addValue("fechaFin", fechaFin);

        return jdbcTemplate.queryForObject(
                sql,
                params,
                BigDecimal.class
        );
    }

    public BigDecimal getTotalDebidoCobrar(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long zonaId
    ) {

        String sql = """
        SELECT
            COALESCE(SUM(ca.total_quota_value), 0)
            +
            COALESCE((
                SELECT SUM(cocd.value)
                FROM credit_other_concepts coc
                INNER JOIN credit_other_concepts_detail cocd
                    ON cocd.credit_other_concepts_id = coc.id
                INNER JOIN credit c3
                    ON c3.id = coc.credit_id
                    AND c3.deleted_at IS NULL
                INNER JOIN credit_intention ci3
                    ON ci3.id = c3.credit_intention_id
                WHERE ci3.zone_id = :zonaId
                  AND cocd.concept_id = 52
                  AND cocd.deleted_at IS NULL
                  AND coc.expiration_date >= :fechaInicio
                  AND coc.expiration_date < :fechaFin
            ), 0)
        FROM credit_amortization ca
        INNER JOIN credit c
            ON c.id = ca.credit_id
            AND c.deleted_at IS NULL
        INNER JOIN credit_intention ci
            ON ci.id = c.credit_intention_id
        WHERE ci.zone_id = :zonaId
          AND ca.expiration_date >= :fechaInicio
          AND ca.expiration_date < :fechaFin
    """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("zonaId", zonaId)
                        .addValue("fechaInicio", fechaInicio)
                        .addValue("fechaFin", fechaFin);

        return jdbcTemplate.queryForObject(
                sql,
                params,
                BigDecimal.class
        );
    }

    public BigDecimal getTotalNoPago(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long zonaId
    ) {

        String sql = """
        SELECT COALESCE(SUM(
            CASE
                WHEN cv.no_pago_count > 0
                THEN ca.total_quota_value
                ELSE 0
            END
        ), 0)
        FROM credit_amortization ca
        INNER JOIN credit c
            ON c.id = ca.credit_id
            AND c.deleted_at IS NULL
        INNER JOIN credit_intention ci
            ON ci.id = c.credit_intention_id
        LEFT JOIN (
            SELECT
                cuota_id,
                SUM(
                    CASE
                        WHEN payment_promise_date IS NOT NULL
                        THEN 1
                        ELSE 0
                    END
                ) AS no_pago_count
            FROM collection_visit
            WHERE visit_date >= :fechaInicio
              AND visit_date < :fechaFin
            GROUP BY cuota_id
        ) cv
            ON ca.id = cv.cuota_id
        WHERE ci.zone_id = :zonaId
          AND ca.expiration_date >= :fechaInicio
          AND ca.expiration_date < :fechaFin
    """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("zonaId", zonaId)
                        .addValue("fechaInicio", fechaInicio)
                        .addValue("fechaFin", fechaFin);

        return jdbcTemplate.queryForObject(
                sql,
                params,
                BigDecimal.class
        );
    }


}