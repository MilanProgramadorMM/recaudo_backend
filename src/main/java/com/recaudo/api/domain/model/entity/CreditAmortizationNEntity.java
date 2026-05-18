package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_amortization")
public class CreditAmortizationNEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_id", nullable = false)
    private Long creditId;

    @Column(name = "quota_number", nullable = false)
    private Integer quotaNumber;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "total_quota_value", nullable = false)
    private BigDecimal totalQuotaValue;

    @Column(name = "liquidated", length = 1)
    @Builder.Default
    private String liquidated = "N";

    @Column(name = "paid_full", length = 1)
    @Builder.Default
    private String paidFull = "N";

}