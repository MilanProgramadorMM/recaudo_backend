package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.DashboardHistorialDto;
import com.recaudo.api.domain.model.dto.response.DashboardNoPagoSummaryDto;
import com.recaudo.api.domain.model.dto.response.DetalleDebidoCobrarDTO;
import com.recaudo.api.infrastructure.helper.sql.QueryBuilder;
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
                    COALESCE(SUM(a.total_quota_value), 0) AS debido_a_cobrar
                FROM credit_amortization a
                INNER JOIN credit c
                    ON c.id = a.credit_id
                INNER JOIN credit_intention ci
                    ON ci.id = c.credit_intention_id
                WHERE c.credit_status = 'ACTIVE'
                  AND c.deleted_at IS NULL
                  AND a.paid_full = 'N'
                  AND ci.zone_id = :zonaId
                  AND a.expiration_date >= :fechaInicio
                  AND a.expiration_date < :fechaFin;
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("zonaId", zonaId)
                        .addValue("fechaInicio", fechaInicio)
                        .addValue("fechaFin", fechaFin);

        // DEBUG
        String sqlDebug = sql
                .replace(":zonaId", String.valueOf(zonaId))
                .replace(":fechaInicio", "'" + fechaInicio + "'")
                .replace(":fechaFin", "'" + fechaFin + "'");

        System.out.println("========== SQL EJECUTADO ==========");
        System.out.println(sqlDebug);
        System.out.println("===================================");

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

    public List<DetalleDebidoCobrarDTO> getDetalleDebidoCobrarPorZona(
            LocalDate startDate, LocalDate endDate, Long zona) {

        String baseSql = """
        SELECT DISTINCT
               c.id                       AS credit_id,
               a.id                       AS cuota_id,
               a.quota_number             AS quota_number,
               a.expiration_date          AS expiration_date,
               ci.fullname                AS client_name,
               pz_cliente.orden           AS client_orden,
               a.total_quota_value        AS valor_cuota,
               z.description              AS zona_code,
               z.value                    AS zona,
               DAYNAME(a.expiration_date) AS nombre_dia
        FROM credit_amortization a
        JOIN credit c               ON c.id = a.credit_id
                                   AND c.credit_status = 'ACTIVE'
        JOIN credit_intention ci    ON ci.id = c.credit_intention_id
        JOIN zona z                 ON z.id = ci.zone_id
        JOIN person_zona pz_cliente ON pz_cliente.zona_id = z.id
                                   AND pz_cliente.person_id = c.person_id
                                   AND pz_cliente.orden > 0
    """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("a.paid_full = :paidFull", "paidFull", "N")
                .addFilter("a.expiration_date >= :startDate", "startDate", startDate)
                .addFilter("a.expiration_date <= :endDate", "endDate", endDate)
                .addFilter("z.id = :zona", "zona", zona)
                .buildWhere()
                .append(" ORDER BY z.value, pz_cliente.orden ");

        System.out.println("SQL: " + qb.getSql());
        System.out.println("PARAMS: " + qb.getParams().getValues());
        return jdbcTemplate.query(qb.getSql(), qb.getParams(),
                (rs, rowNum) -> new DetalleDebidoCobrarDTO(
                        rs.getLong("credit_id"),
                        rs.getLong("cuota_id"),
                        rs.getInt("quota_number"),
                        rs.getObject("expiration_date", LocalDate.class),
                        rs.getString("client_name"),
                        rs.getInt("client_orden"),
                        rs.getBigDecimal("valor_cuota"),
                        rs.getString("zona_code"),
                        rs.getString("zona"),
                        rs.getString("nombre_dia")
                ));
    }
}