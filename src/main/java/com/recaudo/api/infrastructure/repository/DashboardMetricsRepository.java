package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.DashboardHistorialDto;
import com.recaudo.api.domain.model.dto.response.DashboardNoPagoSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    public DashboardNoPagoSummaryDto getTotalNoPago(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long zonaId
    ) {
        // 1. Consulta simplificada apuntando a la nueva tabla analítica
        String sql = """
    SELECT 
        COALESCE(SUM(value), 0) AS total_value,
        COALESCE(SUM(cant), 0)  AS total_cantidad
    FROM dashboard_nopago
    WHERE zona_id = :zonaId
      AND created_at >= :fechaInicio
      AND created_at < :fechaFin
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zonaId", zonaId)
                .addValue("fechaInicio", fechaInicio)
                .addValue("fechaFin", fechaFin);

        // 2. Ejecutamos mapeando el resultado directo al DTO
        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                new DashboardNoPagoSummaryDto(
                        rs.getBigDecimal("total_value"),
                        rs.getLong("total_cantidad")
                )
        );
    }


    // GRAFICOS /////////////////////////////////////////////////////////////////////////////////////////////////

    public List<DashboardHistorialDto> getHistorialDebidoCobrar(
            LocalDate fechaInicio, LocalDate fechaFin, Long zonaId) {

        String sql = """
        SELECT DATE(created_at) AS fecha, SUM(value) AS valor
        FROM dashboard_debido_cobrar
        WHERE zona_id = :zonaId
          AND created_at >= :inicio
          AND created_at < :fin
        GROUP BY DATE(created_at)
        ORDER BY fecha
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zonaId", zonaId)
                .addValue("inicio", fechaInicio.atStartOfDay())
                .addValue("fin", fechaFin.plusDays(1).atStartOfDay());

        return jdbcTemplate.query(sql, params, (rs, row) ->
                DashboardHistorialDto.builder()
                        .fecha(rs.getDate("fecha").toLocalDate())
                        .valor(rs.getBigDecimal("valor"))
                        .build());
    }

    public List<DashboardHistorialDto> getHistorialRecaudado(
            LocalDate fechaInicio, LocalDate fechaFin, Long zonaId) {

        String sql = """
        SELECT DATE(created_at) AS fecha, SUM(value) AS valor
        FROM dashboard_recaudo
        WHERE zona_id = :zonaId
          AND created_at >= :inicio
          AND created_at < :fin
        GROUP BY DATE(created_at)
        ORDER BY fecha
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zonaId", zonaId)
                .addValue("inicio", fechaInicio.atStartOfDay())
                .addValue("fin", fechaFin.plusDays(1).atStartOfDay());

        return jdbcTemplate.query(sql, params, (rs, row) ->
                DashboardHistorialDto.builder()
                        .fecha(rs.getDate("fecha").toLocalDate())
                        .valor(rs.getBigDecimal("valor"))
                        .build());
    }

    public List<DashboardHistorialDto> getHistorialNoPago(
            LocalDate fechaInicio, LocalDate fechaFin, Long zonaId) {

        String sql = """
        SELECT DATE(created_at) AS fecha, SUM(value) AS valor
        FROM dashboard_nopago
        WHERE zona_id = :zonaId
          AND created_at >= :inicio
          AND created_at < :fin
        GROUP BY DATE(created_at)
        ORDER BY fecha
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zonaId", zonaId)
                .addValue("inicio", fechaInicio.atStartOfDay())
                .addValue("fin", fechaFin.plusDays(1).atStartOfDay());

        return jdbcTemplate.query(sql, params, (rs, row) ->
                DashboardHistorialDto.builder()
                        .fecha(rs.getDate("fecha").toLocalDate())
                        .valor(rs.getBigDecimal("valor"))
                        .build());
    }
}