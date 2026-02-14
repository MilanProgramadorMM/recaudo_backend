package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detalle_delay_penalty_agreement")
public class DetalleDelayPenaltyAgreementEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    private DelayPenaltyAgreementEntity agreement;

    @Column(name = "cuota_id")
    private Long cuotaId;

    @Column(name = "days_late")
    private Integer daysLate;

    @Column(name = "pastdue_periods")
    private BigDecimal pastduePeriods;

    @Column(name = "balance_pending")
    private BigDecimal balancePending;

    @Column(name = "delay_penalty")
    private BigDecimal delayPenalty;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}