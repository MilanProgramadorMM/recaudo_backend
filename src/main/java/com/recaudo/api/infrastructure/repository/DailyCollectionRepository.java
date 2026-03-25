package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.domain.model.entity.AmortizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCollectionRepository
        extends JpaRepository<AmortizationEntity, Long> {

    @Query(value = """
        SELECT
             c.id AS creditId,
             a.id AS cuotaId,
             a.quota_number AS quotaNumber,
             a.expiration_date AS expirationDate,
             ci.fullname AS clientName,
             pz_cliente.orden AS clientOrden,
             a.quota_value AS clientCuota,
             z.value AS zona,
             COALESCE(cv.paid, 0) AS paidToday,
             a.paid_full AS paidFull,
             a.liquidated AS liquidated,
             cv.payment_promise_date AS paymentPromiseDate,
             COALESCE(cv.no_pago, 0) AS noPago,
             cv.no_pago_reason AS noPagoReason,
             p.name AS periodo
         FROM amortization a
         JOIN credit c ON c.id = a.credit_id
         JOIN credit_intention ci ON ci.id = c.credit_intention_id
         JOIN period p ON c.period_id = p.id
         JOIN zona z ON z.id = ci.zone_id AND z.value = :zona
         JOIN person_zona pz_asesor
             ON pz_asesor.zona_id = z.id
             AND pz_asesor.orden = 0
         JOIN user u
             ON u.person_id = pz_asesor.person_id
         JOIN person_zona pz_cliente
             ON pz_cliente.zona_id = z.id
             AND pz_cliente.person_id = c.person_id
             AND pz_cliente.orden > 0
         LEFT JOIN collection_visit cv
             ON cv.cuota_id = a.id
             AND cv.visit_date = :fecha
         WHERE a.quota_number = (
            SELECT MIN(a2.quota_number)
            FROM amortization a2
            WHERE a2.credit_id = a.credit_id
            AND a2.paid_full = 'N'
        )
         ORDER BY
             z.value,
             pz_cliente.orden,
             a.expiration_date,
             a.quota_number
    """, nativeQuery = true)
    List<DailyCollectionProjection> findDailyCollection(
            @Param("zona") String zona,
            @Param("fecha") LocalDate fecha
    );
}
