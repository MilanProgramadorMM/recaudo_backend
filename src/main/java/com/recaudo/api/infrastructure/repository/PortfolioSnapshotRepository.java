package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.entity.PortfolioSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshotEntity, Long> {

    @Modifying
    @Query(value = "DELETE FROM portfolio_snapshot WHERE snapshot_date = :fecha", nativeQuery = true)
    int deleteBySnapshotDate(@Param("fecha") LocalDate fecha);

    boolean existsBySnapshotDate(LocalDate fecha);

    long countBySnapshotDate(LocalDate fecha);

    @Query(value = """
        SELECT
            s.id                        AS id,
            s.snapshot_date             AS snapshotDate,
            s.credit_id                 AS creditId,
            s.person_id                 AS personId,
            s.cliente_fullname          AS clienteFullname,
            s.cliente_documento         AS clienteDocumento,
            s.zona_id                   AS zonaId,
            s.zona_nombre               AS zonaNombre,
            s.estado_credito            AS estadoCredito,
            s.credit_line_id            AS creditLineId,
            s.credit_line_nombre        AS creditLineNombre,
            s.period_id                 AS periodId,
            s.period_nombre             AS periodNombre,
            s.period_codigo             AS periodCodigo,
            s.tax_type_id               AS taxTypeId,
            s.tax_type_nombre           AS taxTypeNombre,
            s.tax_value                 AS taxValue,
            s.cuotas_planeadas          AS cuotasPlaneadas,
            s.total_cuotas              AS totalCuotas,
            s.cuotas_pagadas            AS cuotasPagadas,
            s.cuotas_pendientes         AS cuotasPendientes,
            s.capital_generado          AS capitalGenerado,
            s.capital_pagado            AS capitalPagado,
            s.capital_pendiente         AS capitalPendiente,
            s.interes_generado          AS interesGenerado,
            s.interes_pagado            AS interesPagado,
            s.interes_pendiente         AS interesPendiente,
            s.seguro_vida_generado      AS seguroVidaGenerado,
            s.seguro_vida_pagado        AS seguroVidaPagado,
            s.seguro_vida_pendiente     AS seguroVidaPendiente,
            s.seguro_cartera_generado   AS seguroCarteraGenerado,
            s.seguro_cartera_pagado     AS seguroCarteraPagado,
            s.seguro_cartera_pendiente  AS seguroCarteraPendiente,
            s.mora_generada             AS moraGenerada,
            s.mora_pagada               AS moraPagada,
            s.mora_pendiente            AS moraPendiente,
            s.dias_mora                 AS diasMora,
            s.total_pagado              AS totalPagado,
            s.saldo_total               AS saldoTotal,
            s.otros_conceptos_generado  AS otrosConceptosGenerado,
            s.rating_value              AS ratingValue,
            s.rating_range_start        AS ratingRangeStart,
            s.rating_range_end          AS ratingRangeEnd,
            s.created_at                AS createdAt,
            s.job_execution_id          AS jobExecutionId
        FROM portfolio_snapshot s
        WHERE s.snapshot_date = :fecha
        ORDER BY s.credit_id
    """, nativeQuery = true)
    List<PortfolioSnapshotView> findBySnapshotDate(@Param("fecha") LocalDate fecha);

    /**
     * KPIs agregados por zona para una fecha específica.
     * Sirve para el estado de una zona (endpoint 1, lista de un solo zonaId)
     * y de varias zonas (endpoint 2). Una fila por zona.
     */
    @Query(value = """
        SELECT
            MAX(s.snapshot_date)                                              AS snapshotDate,
            s.zona_id                                                         AS zonaId,
            MAX(s.zona_nombre)                                                AS zonaNombre,
            COUNT(*)                                                          AS totalCreditos,
            COUNT(CASE WHEN s.estado_credito = 'ACTIVE'    THEN 1 END)        AS creditosActivos,
            COUNT(CASE WHEN s.estado_credito = 'CANCELLED' THEN 1 END)        AS creditosCancelados,
            COUNT(CASE WHEN s.estado_credito = 'INACTIVE'  THEN 1 END)        AS creditosInactivos,
            COUNT(CASE WHEN s.dias_mora > 0 THEN 1 END)                       AS creditosEnMora,
            COUNT(CASE WHEN COALESCE(s.dias_mora, 0) = 0 THEN 1 END)          AS creditosAlDia,
            COALESCE(SUM(s.capital_generado), 0)                             AS capitalGenerado,
            COALESCE(SUM(s.capital_pagado), 0)                               AS capitalPagado,
            COALESCE(SUM(s.capital_pendiente), 0)                            AS capitalPendiente,
            COALESCE(SUM(s.interes_generado), 0)                             AS interesGenerado,
            COALESCE(SUM(s.interes_pagado), 0)                               AS interesPagado,
            COALESCE(SUM(s.interes_pendiente), 0)                            AS interesPendiente,
            COALESCE(SUM(s.seguro_vida_generado), 0)                         AS seguroVidaGenerado,
            COALESCE(SUM(s.seguro_vida_pagado), 0)                           AS seguroVidaPagado,
            COALESCE(SUM(s.seguro_vida_pendiente), 0)                        AS seguroVidaPendiente,
            COALESCE(SUM(s.seguro_cartera_generado), 0)                      AS seguroCarteraGenerado,
            COALESCE(SUM(s.seguro_cartera_pagado), 0)                        AS seguroCarteraPagado,
            COALESCE(SUM(s.seguro_cartera_pendiente), 0)                     AS seguroCarteraPendiente,
            COALESCE(SUM(s.mora_generada), 0)                               AS moraGenerada,
            COALESCE(SUM(s.mora_pagada), 0)                                 AS moraPagada,
            COALESCE(SUM(s.mora_pendiente), 0)                              AS moraPendiente,
            COALESCE(SUM(s.otros_conceptos_generado), 0)                    AS otrosConceptosGenerado,
            COALESCE(SUM(s.total_pagado), 0)                                AS totalPagado,
            COALESCE(SUM(s.saldo_total), 0)                                 AS saldoTotal,
            COALESCE(SUM(s.cuotas_planeadas), 0)                            AS cuotasPlaneadas,
            COALESCE(SUM(s.total_cuotas), 0)                                AS totalCuotas,
            COALESCE(SUM(s.cuotas_pagadas), 0)                              AS cuotasPagadas,
            COALESCE(SUM(s.cuotas_pendientes), 0)                           AS cuotasPendientes,
            ROUND(AVG(COALESCE(s.dias_mora, 0)), 2)                          AS diasMoraPromedio
        FROM portfolio_snapshot s
        WHERE s.snapshot_date = :fecha
          AND s.zona_id IN (:zonaIds)
        GROUP BY s.zona_id
        ORDER BY s.zona_id
    """, nativeQuery = true)
    List<ZoneAggregateView> findZoneAggregatesByDate(@Param("fecha") LocalDate fecha,
                                                     @Param("zonaIds") List<Long> zonaIds);

    /**
     * Distribución de créditos por calificación (riesgo) por zona en una fecha.
     */
    @Query(value = """
        SELECT
            s.zona_id       AS zonaId,
            s.rating_value  AS ratingValue,
            COUNT(*)        AS cantidad
        FROM portfolio_snapshot s
        WHERE s.snapshot_date = :fecha
          AND s.zona_id IN (:zonaIds)
        GROUP BY s.zona_id, s.rating_value
        ORDER BY s.zona_id, s.rating_value
    """, nativeQuery = true)
    List<ZoneRatingView> findZoneRatingDistributionByDate(@Param("fecha") LocalDate fecha,
                                                          @Param("zonaIds") List<Long> zonaIds);

    /**
     * Serie diaria de KPIs agregados de una zona dentro de un rango de fechas.
     * Una fila por snapshot (día). Base de la serie temporal del endpoint 3.
     */
    @Query(value = """
        SELECT
            s.snapshot_date                                                  AS snapshotDate,
            s.zona_id                                                         AS zonaId,
            MAX(s.zona_nombre)                                                AS zonaNombre,
            COUNT(*)                                                          AS totalCreditos,
            COUNT(CASE WHEN s.estado_credito = 'ACTIVE'    THEN 1 END)        AS creditosActivos,
            COUNT(CASE WHEN s.estado_credito = 'CANCELLED' THEN 1 END)        AS creditosCancelados,
            COUNT(CASE WHEN s.estado_credito = 'INACTIVE'  THEN 1 END)        AS creditosInactivos,
            COUNT(CASE WHEN s.dias_mora > 0 THEN 1 END)                       AS creditosEnMora,
            COUNT(CASE WHEN COALESCE(s.dias_mora, 0) = 0 THEN 1 END)          AS creditosAlDia,
            COALESCE(SUM(s.capital_generado), 0)                             AS capitalGenerado,
            COALESCE(SUM(s.capital_pagado), 0)                               AS capitalPagado,
            COALESCE(SUM(s.capital_pendiente), 0)                            AS capitalPendiente,
            COALESCE(SUM(s.interes_generado), 0)                             AS interesGenerado,
            COALESCE(SUM(s.interes_pagado), 0)                               AS interesPagado,
            COALESCE(SUM(s.interes_pendiente), 0)                            AS interesPendiente,
            COALESCE(SUM(s.seguro_vida_generado), 0)                         AS seguroVidaGenerado,
            COALESCE(SUM(s.seguro_vida_pagado), 0)                           AS seguroVidaPagado,
            COALESCE(SUM(s.seguro_vida_pendiente), 0)                        AS seguroVidaPendiente,
            COALESCE(SUM(s.seguro_cartera_generado), 0)                      AS seguroCarteraGenerado,
            COALESCE(SUM(s.seguro_cartera_pagado), 0)                        AS seguroCarteraPagado,
            COALESCE(SUM(s.seguro_cartera_pendiente), 0)                     AS seguroCarteraPendiente,
            COALESCE(SUM(s.mora_generada), 0)                               AS moraGenerada,
            COALESCE(SUM(s.mora_pagada), 0)                                 AS moraPagada,
            COALESCE(SUM(s.mora_pendiente), 0)                              AS moraPendiente,
            COALESCE(SUM(s.otros_conceptos_generado), 0)                    AS otrosConceptosGenerado,
            COALESCE(SUM(s.total_pagado), 0)                                AS totalPagado,
            COALESCE(SUM(s.saldo_total), 0)                                 AS saldoTotal,
            COALESCE(SUM(s.cuotas_planeadas), 0)                            AS cuotasPlaneadas,
            COALESCE(SUM(s.total_cuotas), 0)                                AS totalCuotas,
            COALESCE(SUM(s.cuotas_pagadas), 0)                              AS cuotasPagadas,
            COALESCE(SUM(s.cuotas_pendientes), 0)                           AS cuotasPendientes,
            ROUND(AVG(COALESCE(s.dias_mora, 0)), 2)                          AS diasMoraPromedio
        FROM portfolio_snapshot s
        WHERE s.zona_id = :zonaId
          AND s.snapshot_date BETWEEN :startDate AND :endDate
        GROUP BY s.snapshot_date, s.zona_id
        ORDER BY s.snapshot_date
    """, nativeQuery = true)
    List<ZoneAggregateView> findZoneDailyAggregates(@Param("zonaId") Long zonaId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Estado ligero por crédito y por día de una zona en un rango, para el análisis
     * de transiciones (entradas/salidas de mora, cambios de estado y de calificación).
     */
    @Query(value = """
        SELECT
            s.snapshot_date   AS snapshotDate,
            s.credit_id       AS creditId,
            s.estado_credito  AS estadoCredito,
            s.dias_mora       AS diasMora,
            s.rating_value    AS ratingValue
        FROM portfolio_snapshot s
        WHERE s.zona_id = :zonaId
          AND s.snapshot_date BETWEEN :startDate AND :endDate
        ORDER BY s.snapshot_date, s.credit_id
    """, nativeQuery = true)
    List<CreditTransitionView> findCreditStatesForTransitions(@Param("zonaId") Long zonaId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    /**
     * Lista/buscador de clientes con cartera en una fecha específica.
     * :busqueda es opcional (null = sin filtro de texto).
     */
    @Query(value = """
    SELECT
        s.person_id                                              AS personId,
        MAX(s.cliente_fullname)                                  AS clienteFullname,
        MAX(s.cliente_documento)                                 AS clienteDocumento,
        COUNT(*)                                                 AS totalCreditos,
        COALESCE(SUM(s.saldo_total), 0)                          AS saldoTotal,
        MAX(s.dias_mora)                                         AS diasMoraMaximo
    FROM portfolio_snapshot s
    WHERE s.snapshot_date = :fecha
      AND (:busqueda IS NULL
           OR s.cliente_fullname LIKE CONCAT('%', :busqueda, '%')
           OR s.cliente_documento LIKE CONCAT('%', :busqueda, '%'))
    GROUP BY s.person_id
    ORDER BY MAX(s.cliente_fullname) ASC
""", nativeQuery = true)
    List<ClientListView> findClientsBySnapshotDate(@Param("fecha") LocalDate fecha,
                                                   @Param("busqueda") String busqueda);

    /**
     * Estado agregado de un cliente (todos sus créditos) en una fecha puntual.
     */
    @Query(value = """
    SELECT
        s.person_id                                                       AS personId,
        MAX(s.cliente_fullname)                                           AS clienteFullname,
        MAX(s.cliente_documento)                                          AS clienteDocumento,
        GROUP_CONCAT(DISTINCT s.zona_nombre ORDER BY s.zona_nombre SEPARATOR ', ') AS zonaNombres,
        COUNT(*)                                                          AS totalCreditos,
        COUNT(CASE WHEN s.estado_credito = 'ACTIVE'    THEN 1 END)        AS creditosActivos,
        COUNT(CASE WHEN s.estado_credito = 'CANCELLED' THEN 1 END)        AS creditosCancelados,
        COUNT(CASE WHEN s.estado_credito = 'INACTIVE'  THEN 1 END)        AS creditosInactivos,
        COUNT(CASE WHEN s.dias_mora > 0 THEN 1 END)                       AS creditosEnMora,
        COUNT(CASE WHEN COALESCE(s.dias_mora, 0) = 0 THEN 1 END)          AS creditosAlDia,
        COALESCE(SUM(s.capital_generado), 0)                             AS capitalGenerado,
        COALESCE(SUM(s.capital_pagado), 0)                               AS capitalPagado,
        COALESCE(SUM(s.capital_pendiente), 0)                            AS capitalPendiente,
        COALESCE(SUM(s.interes_generado), 0)                             AS interesGenerado,
        COALESCE(SUM(s.interes_pagado), 0)                               AS interesPagado,
        COALESCE(SUM(s.interes_pendiente), 0)                            AS interesPendiente,
        COALESCE(SUM(s.seguro_vida_generado), 0)                         AS seguroVidaGenerado,
        COALESCE(SUM(s.seguro_vida_pagado), 0)                           AS seguroVidaPagado,
        COALESCE(SUM(s.seguro_vida_pendiente), 0)                        AS seguroVidaPendiente,
        COALESCE(SUM(s.seguro_cartera_generado), 0)                      AS seguroCarteraGenerado,
        COALESCE(SUM(s.seguro_cartera_pagado), 0)                        AS seguroCarteraPagado,
        COALESCE(SUM(s.seguro_cartera_pendiente), 0)                     AS seguroCarteraPendiente,
        COALESCE(SUM(s.mora_generada), 0)                               AS moraGenerada,
        COALESCE(SUM(s.mora_pagada), 0)                                 AS moraPagada,
        COALESCE(SUM(s.mora_pendiente), 0)                              AS moraPendiente,
        COALESCE(SUM(s.otros_conceptos_generado), 0)                    AS otrosConceptosGenerado,
        COALESCE(SUM(s.total_pagado), 0)                                AS totalPagado,
        COALESCE(SUM(s.saldo_total), 0)                                 AS saldoTotal,
        COALESCE(SUM(s.cuotas_planeadas), 0)                            AS cuotasPlaneadas,
        COALESCE(SUM(s.total_cuotas), 0)                                AS totalCuotas,
        COALESCE(SUM(s.cuotas_pagadas), 0)                              AS cuotasPagadas,
        COALESCE(SUM(s.cuotas_pendientes), 0)                           AS cuotasPendientes,
        MAX(s.dias_mora)                                                 AS diasMoraMaximo,
        ROUND(AVG(COALESCE(s.dias_mora, 0)), 2)                          AS diasMoraPromedio
    FROM portfolio_snapshot s
    WHERE s.snapshot_date = :fecha
      AND s.person_id = :personId
    GROUP BY s.person_id
""", nativeQuery = true)
    Optional<ClientAggregateView> findClientAggregateByDate(@Param("fecha") LocalDate fecha,
                                                            @Param("personId") Long personId);

    /**
     * Serie diaria de KPIs agregados de un cliente dentro de un rango de fechas.
     * Una fila por snapshot (día). Base de la serie temporal del historial.
     */
    @Query(value = """
    SELECT
        s.snapshot_date                                                    AS snapshotDate,
        s.person_id                                                        AS personId,
        MAX(s.cliente_fullname)                                            AS clienteFullname,
        MAX(s.cliente_documento)                                           AS clienteDocumento,
        GROUP_CONCAT(DISTINCT s.zona_nombre ORDER BY s.zona_nombre SEPARATOR ', ') AS zonaNombres,
        COUNT(*)                                                          AS totalCreditos,
        COUNT(CASE WHEN s.estado_credito = 'ACTIVE'    THEN 1 END)        AS creditosActivos,
        COUNT(CASE WHEN s.estado_credito = 'CANCELLED' THEN 1 END)        AS creditosCancelados,
        COUNT(CASE WHEN s.estado_credito = 'INACTIVE'  THEN 1 END)        AS creditosInactivos,
        COUNT(CASE WHEN s.dias_mora > 0 THEN 1 END)                       AS creditosEnMora,
        COUNT(CASE WHEN COALESCE(s.dias_mora, 0) = 0 THEN 1 END)          AS creditosAlDia,
        COALESCE(SUM(s.capital_generado), 0)                             AS capitalGenerado,
        COALESCE(SUM(s.capital_pagado), 0)                               AS capitalPagado,
        COALESCE(SUM(s.capital_pendiente), 0)                            AS capitalPendiente,
        COALESCE(SUM(s.interes_generado), 0)                             AS interesGenerado,
        COALESCE(SUM(s.interes_pagado), 0)                               AS interesPagado,
        COALESCE(SUM(s.interes_pendiente), 0)                            AS interesPendiente,
        COALESCE(SUM(s.seguro_vida_generado), 0)                         AS seguroVidaGenerado,
        COALESCE(SUM(s.seguro_vida_pagado), 0)                           AS seguroVidaPagado,
        COALESCE(SUM(s.seguro_vida_pendiente), 0)                        AS seguroVidaPendiente,
        COALESCE(SUM(s.seguro_cartera_generado), 0)                      AS seguroCarteraGenerado,
        COALESCE(SUM(s.seguro_cartera_pagado), 0)                        AS seguroCarteraPagado,
        COALESCE(SUM(s.seguro_cartera_pendiente), 0)                     AS seguroCarteraPendiente,
        COALESCE(SUM(s.mora_generada), 0)                               AS moraGenerada,
        COALESCE(SUM(s.mora_pagada), 0)                                 AS moraPagada,
        COALESCE(SUM(s.mora_pendiente), 0)                              AS moraPendiente,
        COALESCE(SUM(s.otros_conceptos_generado), 0)                    AS otrosConceptosGenerado,
        COALESCE(SUM(s.total_pagado), 0)                                AS totalPagado,
        COALESCE(SUM(s.saldo_total), 0)                                 AS saldoTotal,
        COALESCE(SUM(s.cuotas_planeadas), 0)                            AS cuotasPlaneadas,
        COALESCE(SUM(s.total_cuotas), 0)                                AS totalCuotas,
        COALESCE(SUM(s.cuotas_pagadas), 0)                              AS cuotasPagadas,
        COALESCE(SUM(s.cuotas_pendientes), 0)                           AS cuotasPendientes,
        MAX(s.dias_mora)                                                 AS diasMoraMaximo,
        ROUND(AVG(COALESCE(s.dias_mora, 0)), 2)                          AS diasMoraPromedio
    FROM portfolio_snapshot s
    WHERE s.person_id = :personId
      AND s.snapshot_date BETWEEN :startDate AND :endDate
    GROUP BY s.snapshot_date, s.person_id
    ORDER BY s.snapshot_date
""", nativeQuery = true)
    List<ClientAggregateView> findClientDailyAggregates(@Param("personId") Long personId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}