package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.consultas.DebidoCobrarDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DefaultConsultasDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleCreditosPorZona;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleMovimientoPorZonaDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleSaldoVencidoDTO;
import com.recaudo.api.domain.model.dto.response.consultas.MovimientoPorZonaDTO;
import com.recaudo.api.infrastructure.helper.sql.QueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
@AllArgsConstructor
public class ConsultasRepository {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<MovimientoPorZonaDTO> getMovimientosPorZona(
            Long concept,
            Long paymentType,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        String baseSql = """
        SELECT
            z.id AS zone_id,
            z.value AS zone_name,

            SUM(r.value_paid) * -1 AS total_recaudado,

            SUM(COALESCE(d.total_capital, 0)) * -1 AS total_capital,
            SUM(COALESCE(d.total_interes, 0)) * -1 AS total_interes,
            SUM(COALESCE(d.total_seguro_vida, 0)) * -1 AS total_seguro_vida,
            SUM(COALESCE(d.total_seguro_cartera, 0)) * -1 AS total_seguro_cartera

        FROM new_recaudo r

        INNER JOIN credit c
            ON c.id = r.credit_id

        INNER JOIN credit_intention ci
            ON ci.id = c.credit_intention_id

        INNER JOIN zona z
            ON z.id = ci.zone_id

        LEFT JOIN (
            SELECT
                recaudo_id,

                SUM(CASE WHEN concept_id = 48 THEN value ELSE 0 END) AS total_capital,
                SUM(CASE WHEN concept_id = 49 THEN value ELSE 0 END) AS total_interes,
                SUM(CASE WHEN concept_id = 50 THEN value ELSE 0 END) AS total_seguro_vida,
                SUM(CASE WHEN concept_id = 51 THEN value ELSE 0 END) AS total_seguro_cartera

            FROM new_recaudo_detail
            GROUP BY recaudo_id
        ) d
            ON d.recaudo_id = r.id
        """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("r.created_at >= :startDate", "startDate", startDate)
                .addFilter("r.created_at < :endDate", "endDate", endDate)
                .buildWhere()
                .append(" GROUP BY z.id, z.value ")
                .append(" ORDER BY z.value ");

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new MovimientoPorZonaDTO(
                        rs.getLong("zone_id"),
                        rs.getString("zone_name"),
                        rs.getBigDecimal("total_recaudado"),
                        rs.getBigDecimal("total_capital"),
                        rs.getBigDecimal("total_interes"),
                        rs.getBigDecimal("total_seguro_vida"),
                        rs.getBigDecimal("total_seguro_cartera")
                )
        );
    }

    public List<DetalleMovimientoPorZonaDTO> getDetalleMovimientosPorZona(Long concept, Long paymentType, LocalDateTime startDate, LocalDateTime endDate, Long zona) {
        String baseSql = """
    SELECT
        z.value AS zona,

        tr.concept_key,
        tr.name AS concept,

        g.name AS payment_type,

        r.value_paid,

        COALESCE(d.investment_value,0) AS investment_value,
        COALESCE(d.interest_value,0) AS interest_value,
        COALESCE(d.life_insurance,0) AS life_insurance,
        COALESCE(d.portfolio_insurance,0) AS portfolio_insurance,

        r.created_at,
        r.user_create

    FROM new_recaudo r

    INNER JOIN credit c
        ON c.id = r.credit_id

    INNER JOIN credit_intention ci
        ON ci.id = c.credit_intention_id

    INNER JOIN zona z
        ON z.id = ci.zone_id

    LEFT JOIN glotypes g
        ON g.id = r.payment_type_id

    LEFT JOIN (
        SELECT
            nd.recaudo_id,

            MAX(nd.type_recaudo_id) AS type_recaudo_id,

            SUM(CASE WHEN nd.concept_id = 48 THEN nd.value ELSE 0 END) AS investment_value,
            SUM(CASE WHEN nd.concept_id = 49 THEN nd.value ELSE 0 END) AS interest_value,
            SUM(CASE WHEN nd.concept_id = 50 THEN nd.value ELSE 0 END) AS life_insurance,
            SUM(CASE WHEN nd.concept_id = 51 THEN nd.value ELSE 0 END) AS portfolio_insurance

        FROM new_recaudo_detail nd
        GROUP BY nd.recaudo_id
    ) d
        ON d.recaudo_id = r.id

    LEFT JOIN concept tr
        ON tr.id = d.type_recaudo_id
    """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("ci.zone_id = :zona", "zona", zona)
                .addFilter("d.type_recaudo_id = :concept", "concept", concept)
                .addFilter("r.payment_type_id = :paymentType", "paymentType", paymentType)
                .addFilter("r.created_at >= :startDate", "startDate", startDate)
                .addFilter("r.created_at < :endDate", "endDate", endDate)
                .buildWhere();

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DetalleMovimientoPorZonaDTO(
                        rs.getString("zona"),
                        rs.getString("concept_key"),
                        rs.getString("concept"),
                        rs.getString("payment_type"),
                        rs.getBigDecimal("value_paid"),
                        rs.getBigDecimal("investment_value"),
                        rs.getBigDecimal("interest_value"),
                        rs.getBigDecimal("life_insurance"),
                        rs.getBigDecimal("portfolio_insurance"),
                        rs.getObject("created_at", LocalDateTime.class),
                        rs.getString("user_create")
                )
        );
    }

    public List<DefaultConsultasDTO> getSaldoVencidoPorZona(LocalDateTime startDate, LocalDateTime endDate) {
        String baseSql = """
            SELECT
                z.id,
                z.value AS zona,
                COALESCE(SUM(a.quota_value), 0)
                + COALESCE(SUM(r.total_paid), 0) AS value
            FROM zona z
            LEFT JOIN credit_intention ci ON ci.zone_id = z.id
            LEFT JOIN credit c ON c.credit_intention_id = ci.id
            LEFT JOIN amortization a
                ON a.credit_id = c.id
                AND a.expiration_date < :endDate
                AND a.paid_full = 'N'
            LEFT JOIN (
                SELECT cuota_id, SUM(value_paid) AS total_paid
                FROM recaudo
                WHERE value_paid < 0
                GROUP BY cuota_id
            ) r ON r.cuota_id = a.id
        """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("", "endDate", endDate)
                .buildWhere()
                .append("GROUP BY z.id, z.value")
                .append("ORDER BY z.value");

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DefaultConsultasDTO(
                        rs.getLong("id"),
                        rs.getString("zona"),
                        rs.getBigDecimal("value")
                )
        );
    }

    public List<DetalleSaldoVencidoDTO> getDetalleSaldoVencidoPorZona(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long zona) {

        String baseSql = """
    WITH recaudos_agrupados AS (
        SELECT
            quota_id,
            SUM(value_paid) AS total_paid
        FROM new_recaudo
        WHERE value_paid < 0
        GROUP BY quota_id
    ),
    mora_agrupada AS (
        SELECT
            coc.credit_id,
            coc.quota_number,
            SUM(cocd.value) AS mora_real
        FROM credit_other_concepts coc
        INNER JOIN credit_other_concepts_detail cocd
            ON cocd.credit_other_concepts_id = coc.id
        GROUP BY
            coc.credit_id,
            coc.quota_number
    )
    SELECT
        c.id AS credit_id,
        ci.id AS credit_intention_id,
        z.value AS zona,
        p.fullname AS person_name,

        COALESCE(SUM(a.total_quota_value), 0)
            + COALESCE(SUM(r.total_paid), 0) AS value,

        MAX(
            DATEDIFF(CURDATE(), a.expiration_date)
        ) AS dias_mora,

        MAX(
            DATEDIFF(CURDATE(), a.expiration_date)
        ) / 30 AS periodos_vencidos,

        COALESCE(
            SUM(m.mora_real),
            0
        ) AS interes_moratorio,

        MAX(
            DATEDIFF(CURDATE(), a.expiration_date)
        ) > 0 AS is_overdue

    FROM zona z

    LEFT JOIN credit_intention ci
        ON ci.zone_id = z.id

    LEFT JOIN credit c
        ON c.credit_intention_id = ci.id

    LEFT JOIN person p
        ON c.person_id = p.id

    LEFT JOIN credit_amortization a
        ON a.credit_id = c.id
        AND a.expiration_date < :endDate
        AND a.paid_full = 'N'

    LEFT JOIN recaudos_agrupados r
        ON r.quota_id = a.id

    LEFT JOIN mora_agrupada m
        ON m.credit_id = a.credit_id
        AND m.quota_number = a.quota_number
    """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("", "startDate", startDate)
                .addFilter("", "endDate", endDate)
                .addFilter("z.id = :zone", "zone", zona)
                .buildWhere()
                .append("""
            GROUP BY
                z.id,
                z.value,
                p.id,
                p.fullname,
                c.id,
                ci.id
            """)
                .append("""
            HAVING
                COALESCE(SUM(a.total_quota_value), 0)
                + COALESCE(SUM(r.total_paid), 0) > 0
            """)
                .append("""
            ORDER BY
                MAX(DATEDIFF(CURDATE(), a.expiration_date)) DESC
            """);

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DetalleSaldoVencidoDTO(
                        rs.getLong("credit_id"),
                        rs.getLong("credit_intention_id"),
                        rs.getString("zona"),
                        rs.getString("person_name"),
                        rs.getBigDecimal("value"),
                        rs.getInt("dias_mora"),
                        rs.getDouble("periodos_vencidos"),
                        rs.getBigDecimal("interes_moratorio"),
                        rs.getBoolean("is_overdue")
                )
        );
    }

    public List<DefaultConsultasDTO> getCreditosPorZona(LocalDateTime startDate, LocalDateTime endDate) {
        String baseSql = """
            SELECT
                z.id,
                z.value,
                (
                    SELECT COUNT(1)
                    FROM credit c
                    INNER JOIN credit_intention ci ON c.credit_intention_id = ci.id
                    WHERE ci.zone_id = z.id
                    AND c.created_at >= :startDate AND c.created_at < :endDate
                ) AS creditos
            FROM zona z
        """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("", "startDate", startDate)
                .addFilter("", "endDate", endDate)
                .buildWhere()
                .append("GROUP BY z.id, z.value")
                .append("ORDER BY z.value ASC");

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DefaultConsultasDTO(
                        rs.getLong("id"),
                        rs.getString("value"),
                        rs.getBigDecimal("creditos")
                )
        );
    }

    public List<DetalleCreditosPorZona> getDetalleCreditosPorZona(LocalDateTime startDate, LocalDateTime endDate, Long zona) {
        String baseSql = """
            SELECT
                c.id AS credit_id,
                c.credit_intention_id,
                CONCAT(p.document,' - ', p.fullname) AS fullname,
                p2.name AS period,
                c.period_quantity,
                c.quota_value,
                cl.name AS credit_line,
                c.total_capital_value,
                c.initial_value_payment,
                c.total_financed_value,
                c.total_intention_value,
                c.total_interest_value,
                c.item_value,
                COALESCE(c.stationery, 0) AS stationery
            FROM credit AS c
            INNER JOIN person AS p ON c.person_id = p.id
            INNER JOIN credit_line AS cl ON c.credit_line_id = cl.id
            INNER JOIN period AS p2 ON c.period_id = p2.id
            INNER JOIN credit_intention AS ci ON c.credit_intention_id = ci.id
            INNER JOIN zona AS z ON ci.zone_id = z.id
        """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("c.created_at >= :startDate", "startDate", startDate)
                .addFilter("c.created_at < :endDate", "endDate", endDate)
                .addFilter("z.id = :zona", "zona", zona)
                .buildWhere();

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DetalleCreditosPorZona(
                        rs.getLong("credit_id"),
                        rs.getLong("credit_intention_id"),
                        rs.getString("fullname"),
                        rs.getString("period"),
                        rs.getInt("period_quantity"),
                        rs.getBigDecimal("quota_value"),
                        rs.getString("credit_line"),
                        rs.getBigDecimal("total_capital_value"),
                        rs.getBigDecimal("initial_value_payment"),
                        rs.getBigDecimal("total_financed_value"),
                        rs.getBigDecimal("total_intention_value"),
                        rs.getBigDecimal("total_interest_value"),
                        rs.getBigDecimal("item_value"),
                        rs.getBigDecimal("stationery")
                )
        );
    }

    public List<DebidoCobrarDTO> getDebidoCobrarPorZona(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        String baseSql = """
    SELECT
        z.id,
        z.value AS zona,

        SUM(a.total_quota_value) AS quota_value,

        SUM(
            COALESCE(ad.interest_value, 0)
        ) AS interest_value,

        SUM(
            COALESCE(ad.investment_value, 0)
        ) AS investment_value

    FROM credit_amortization a

    LEFT JOIN (
        SELECT
            amortization_id,

            SUM(
                CASE
                    WHEN concept_id = 48
                    THEN value
                    ELSE 0
                END
            ) AS investment_value,

            SUM(
                CASE
                    WHEN concept_id = 49
                    THEN value
                    ELSE 0
                END
            ) AS interest_value

        FROM credit_amortization_detail
        GROUP BY amortization_id
    ) ad
        ON ad.amortization_id = a.id

    INNER JOIN credit c
        ON a.credit_id = c.id

    INNER JOIN credit_intention ci
        ON c.credit_intention_id = ci.id

    INNER JOIN zona z
        ON ci.zone_id = z.id
    """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("a.expiration_date >= :startDate", "startDate", startDate)
                .addFilter("a.expiration_date < :endDate", "endDate", endDate)
                .buildWhere()
                .append("GROUP BY z.id, z.value")
                .append("ORDER BY z.value");

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DebidoCobrarDTO(
                        rs.getLong("id"),
                        rs.getString("zona"),
                        rs.getBigDecimal("quota_value"),
                        rs.getBigDecimal("interest_value"),
                        rs.getBigDecimal("investment_value")
                )
        );
    }


    public List<DebidoCobrarDTO> getDebidoCobrarPorZona(LocalDateTime startDate, LocalDateTime endDate, Long zona) {
        String baseSql = """
            SELECT  z.id, z.value AS zona,
                    SUM(a.quota_value) AS quota_value,
                    SUM(a.interest_value) AS interest_value,
                    SUM(a.investment_value) AS investment_value
            FROM amortization AS a
            INNER JOIN credit AS c ON a.credit_id = c.id
            INNER JOIN credit_intention AS ci ON c.credit_intention_id = ci.id
            INNER JOIN zona AS z ON ci.zone_id = z.id
        """;

        QueryBuilder qb = new QueryBuilder(baseSql)
                .addFilter("a.expiration_date >= :startDate", "startDate", startDate)
                .addFilter("a.expiration_date < :endDate", "endDate", endDate)
                .addFilter("z.id = :zona", "zona", zona)
                .buildWhere()
                .append("GROUP BY z.id")
                .append("ORDER BY z.value");

        return jdbcTemplate.query(
                qb.getSql(),
                qb.getParams(),
                (rs, rowNum) -> new DebidoCobrarDTO(
                        rs.getLong("id"),
                        rs.getString("zona"),
                        rs.getBigDecimal("quota_value"),
                        rs.getBigDecimal("interest_value"),
                        rs.getBigDecimal("investment_value")
                )
        );
    }


}
