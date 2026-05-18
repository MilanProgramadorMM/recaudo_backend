package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_intention_amortization_detail")
public class CreditIntentionAmortizationDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amortization_id", nullable = false)
    private Long amortizationId;

    @Column(name = "concept_id", nullable = false)
    private Integer conceptId;

    @Column(name = "value", nullable = false)
    private BigDecimal value;
}