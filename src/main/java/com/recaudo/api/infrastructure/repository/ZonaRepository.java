package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.DailyReportDetailDto;
import com.recaudo.api.domain.model.dto.response.DailyReportSummaryDto;
import com.recaudo.api.domain.model.entity.ZonaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ZonaRepository extends JpaRepository<ZonaEntity, Long> {

    ZonaEntity findByValue(String value);
    boolean existsByValueIgnoreCaseAndIdNot(String value, Long id);
    List<ZonaEntity> findAllByStatusTrue(Sort sort);
    /**
     * Obtiene el resumen diario por zona
     */
    @Query(value = """
        SELECT
            z.id AS zonaId,
            z.value AS zonaName,
            COALESCE(COUNT(DISTINCT a.id), 0) AS clientesEnLista,
            COALESCE(COUNT(DISTINCT CASE WHEN cv.id IS NOT NULL THEN cv.cuota_id END), 0) AS clientesVisitados,
            COALESCE(COUNT(DISTINCT CASE WHEN cv.paid = 1 THEN cv.cuota_id END), 0) AS clientesPagaron,
            COALESCE(COUNT(DISTINCT CASE WHEN cv.no_pago = 1 THEN cv.cuota_id END), 0) AS clientesNoPagaron,
            COALESCE(COUNT(DISTINCT CASE
                WHEN cv.payment_promise_date IS NOT NULL
                AND cv.paid = 0
                AND cv.no_pago = 0
                THEN cv.cuota_id
            END), 0) AS clientesPromesa,
            COALESCE(COUNT(DISTINCT CASE WHEN cv.id IS NULL THEN a.id END), 0) AS clientesPendientes,
            COALESCE(SUM(ABS(r.value_paid)), 0) AS totalRecaudado
        FROM zona z
        LEFT JOIN credit_intention ci ON ci.zone_id = z.id
        LEFT JOIN credit c ON c.credit_intention_id = ci.id
        LEFT JOIN amortization a ON a.credit_id = c.id
            AND a.paid_full = 'N'
            AND a.expiration_date <= :fecha
        LEFT JOIN collection_visit cv ON cv.cuota_id = a.id
            AND cv.visit_date = :fecha
        LEFT JOIN recaudo r ON r.cuota_id = a.id
            AND DATE(r.created_at) = :fecha
        WHERE z.value = :zonaName
        GROUP BY z.id, z.value
    """, nativeQuery = true)
    Optional<DailyReportSummaryDto> getDailySummaryByZone(
            @Param("zonaName") String zonaName,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Obtiene el detalle diario de recaudos por zona
     */
    @Query(value = """
        SELECT
            ci.fullname AS clientName,
            pz_cliente.orden AS clientOrden,
            a.quota_number AS quotaNumber,
            a.quota_value AS quotaValue,
            a.expiration_date AS expirationDate,
            CASE
                WHEN cv.paid = 1 THEN 'PAGADO'
                WHEN cv.no_pago = 1 THEN 'NO_PAGO'
                WHEN cv.payment_promise_date IS NOT NULL THEN 'PROMESA'
                ELSE 'SIN_VISITA'
            END AS estado,
            cv.no_pago_reason AS motivoNoPago,
            cv.observation AS observacion,
            cv.payment_promise_date AS fechaPromesa,
            COALESCE(
                (SELECT SUM(ABS(r.value_paid))
                 FROM recaudo r
                 WHERE r.cuota_id = a.id
                 AND DATE(r.created_at) = :fecha),
                0
            ) AS montoRecaudado
        FROM amortization a
        JOIN credit c ON c.id = a.credit_id
        JOIN credit_intention ci ON ci.id = c.credit_intention_id
        JOIN zona z ON z.id = ci.zone_id
        JOIN person_zona pz_asesor
            ON pz_asesor.zona_id = z.id
            AND pz_asesor.orden = 0
        JOIN user u
            ON u.person_id = pz_asesor.person_id
            AND u.username = :username
        JOIN person_zona pz_cliente
            ON pz_cliente.zona_id = z.id
            AND pz_cliente.person_id = c.person_id
            AND pz_cliente.orden > 0
        LEFT JOIN collection_visit cv
            ON cv.cuota_id = a.id
            AND cv.visit_date = :fecha
        WHERE
            a.paid_full = 'N'
            AND a.expiration_date <= :fecha
        ORDER BY
            pz_cliente.orden,
            a.quota_number
    """, nativeQuery = true)
    List<DailyReportDetailDto> getDailyDetailByZone(
            @Param("username") String username,
            @Param("fecha") LocalDate fecha
    );
}
