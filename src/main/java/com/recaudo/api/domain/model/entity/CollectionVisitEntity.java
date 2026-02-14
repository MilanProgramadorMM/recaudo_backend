package com.recaudo.api.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "collection_visit",
    uniqueConstraints = {
        // Evita duplicar visitas el mismo día para la misma cuota
        @UniqueConstraint(columnNames = {"cuota_id", "visit_date"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionVisitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_id", nullable = false)
    private Long creditId;

    @Column(name = "cuota_id", nullable = false)
    private Long cuotaId;

    // Día exacto de la visita
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    // Usuario del asesor que hizo la visita
    @Column(name = "advisor_username", nullable = false)
    private String advisorUsername;

    // Indica si HOY se pagó algo
    // false NO significa que no deba, solo que HOY no pagó
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;

    // Si el cliente promete pagar otro día
    @Column(name = "payment_promise_date")
    private LocalDate paymentPromiseDate;

    @Column(name = "no_pago", nullable = false)
    private Boolean noPago = false;

    // Motivo por el cual no pagó
    @Column(name = "no_pago_reason")
    private String noPagoReason;

    @Column(name = "observation")
    private String observation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
