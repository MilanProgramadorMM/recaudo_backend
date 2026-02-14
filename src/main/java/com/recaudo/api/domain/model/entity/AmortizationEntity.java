package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "amortization")
public class AmortizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_id")
    private Long creditId;

    @Column(name = "quota_number")
    private Integer quotaNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "capital_balance")
    private BigDecimal capitalBalance;

    @Column(name = "investment_value")
    private BigDecimal investmentValue;

    @Column(name = "interest_value")
    private BigDecimal interestValue;

    @Column(name = "life_insurance")
    private BigDecimal lifeInsurance;

    @Column(name = "portfolio_insurance")
    private BigDecimal portfolioInsurance;

    @Column(name = "liquidated")
    private String liquidated;

    @Column(name = "paid_full")
    private String paidFull;

    @Column(name = "quota_value")
    private BigDecimal quotaValue;
}