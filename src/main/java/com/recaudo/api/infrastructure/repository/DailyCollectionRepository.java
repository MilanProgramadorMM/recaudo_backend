package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.domain.model.dto.response.DailyCollectionRespaldoProjection;
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
             c.created_at AS fechaCredito,
             a.id AS cuotaId,
             a.quota_number AS quotaNumber,
             a.expiration_date AS expirationDate,
             ci.fullname AS clientName,
             pz_cliente.orden AS clientOrden,
             z.value AS zona,
             COALESCE(cv.paid, 0) AS paidToday,
             a.paid_full AS paidFull,
             a.liquidated AS liquidated,
             cv.payment_promise_date AS paymentPromiseDate,
             COALESCE(cv.no_pago, 0) AS noPago,
             cv.no_pago_reason AS noPagoReason,
             p.name AS periodo,
             c.period_quantity AS plazoCredito,
             c.quota_value AS valorCuota,
             (SELECT COUNT(*) FROM amortization a3
              WHERE a3.credit_id = c.id AND a3.paid_full = 'S') AS cuotasPagadas,
             (SELECT COUNT(*) FROM amortization a4
              WHERE a4.credit_id = c.id
                AND a4.paid_full = 'N'
                AND a4.expiration_date < CURDATE()) AS cuotasVencidas,
             (SELECT COALESCE(SUM(a5.quota_value), 0) FROM amortization a5
              WHERE a5.credit_id = c.id AND a5.paid_full = 'N') AS saldoPendiente,
             (SELECT COUNT(*) FROM amortization a6
              WHERE a6.credit_id = c.id) AS totalCuotas,
             (SELECT ci2.value FROM contact_info ci2
              WHERE ci2.person = c.person_id AND ci2.type = 10
                AND ci2.deleted_at IS NULL LIMIT 1) AS direccion,
             (SELECT ci3.value FROM contact_info ci3
              WHERE ci3.person = c.person_id AND ci3.type = 13
                AND ci3.deleted_at IS NULL LIMIT 1) AS telefono,
             (SELECT b.value FROM barrio b WHERE b.id = ci.neighborhood_id LIMIT 1) AS barrio,
             (SELECT m.value FROM municipio m WHERE m.id = ci.municipality_id LIMIT 1) AS municipio,
             DAYNAME(a.expiration_date) AS nombreDia
         FROM amortization a
         JOIN credit c ON c.id = a.credit_id
         JOIN credit_intention ci ON ci.id = c.credit_intention_id
         JOIN period p ON c.period_id = p.id
         JOIN zona z ON z.id = ci.zone_id AND z.id = :zona
         JOIN person_zona pz_asesor
             ON pz_asesor.zona_id = z.id AND pz_asesor.orden = 0
         JOIN user u ON u.person_id = pz_asesor.person_id
         JOIN person_zona pz_cliente
             ON pz_cliente.zona_id = z.id
             AND pz_cliente.person_id = c.person_id
             AND pz_cliente.orden > 0
         LEFT JOIN collection_visit cv
             ON cv.cuota_id = a.id AND cv.visit_date = :fecha
         WHERE (
             -- PRIORIDAD 1: cuota que fue visitada hoy (pagada, promesa o no pago)
             a.id IN (
                 SELECT cv2.cuota_id
                 FROM collection_visit cv2
                 WHERE cv2.credit_id = c.id
                   AND cv2.visit_date = :fecha
             )
             OR
             -- PRIORIDAD 2: mínima cuota pendiente que NO fue visitada hoy
             (
                 a.quota_number = (
                     SELECT MIN(a2.quota_number)
                     FROM amortization a2
                     WHERE a2.credit_id = a.credit_id
                       AND a2.paid_full = 'N'
                 )
                 AND NOT EXISTS (
                     SELECT 1 FROM collection_visit cv3
                     WHERE cv3.credit_id = c.id
                       AND cv3.visit_date = :fecha
                 )
             )
         )
         ORDER BY z.value, pz_cliente.orden, a.expiration_date, a.quota_number
    """, nativeQuery = true)
    List<DailyCollectionProjection> findDailyCollection(
            @Param("zona") Long zona,
            @Param("fecha") LocalDate fecha
    );

    @Query(value = """
        WITH pagos AS (
          SELECT
              credit_id,
              cuota_id,
              value_paid,
              created_at,
              LAG(created_at) OVER (
                  PARTITION BY credit_id
                  ORDER BY created_at
              ) AS prev_time
          FROM recaudo
          WHERE credit_id IN (:credits)
      ),
      grupos AS (
          SELECT *,
              CASE
                  WHEN prev_time IS NULL
                       OR TIMESTAMPDIFF(SECOND, prev_time, created_at) > 300
                  THEN 1 ELSE 0
              END AS nuevo_grupo
          FROM pagos
      ),
      grupo_final AS (
          SELECT *,
              SUM(nuevo_grupo) OVER (
                  PARTITION BY credit_id
                  ORDER BY created_at
              ) AS grupo_id
          FROM grupos
      )
      SELECT
          credit_id,
          grupo_id,
          MIN(created_at) AS fecha_inicio,
          MAX(created_at) AS fecha_fin,
          SUM(value_paid) AS total_pagado,
          DAYNAME(MIN(created_at)) AS nombre_dia
      FROM grupo_final
      GROUP BY credit_id, grupo_id
      ORDER BY fecha_inicio;
    """, nativeQuery = true)
    List<DailyCollectionRespaldoProjection> finDailyCollectionRespaldo(@Param("credits") List<Long> credits);
}
