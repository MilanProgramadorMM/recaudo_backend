package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delay_penalty_agreement")
public class DelayPenaltyAgreementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_id")
    private Long creditId;

    @Column(name = "projected_value")
    private BigDecimal projectedValue;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "agreed_value")
    private BigDecimal agreedValue;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "user_update")
    private String userUpdate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}