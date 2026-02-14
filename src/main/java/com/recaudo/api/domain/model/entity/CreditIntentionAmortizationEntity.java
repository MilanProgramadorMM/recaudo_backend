package com.recaudo.api.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "credit_intention_amortization")
public class CreditIntentionAmortizationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_intention_id")
    private Long creditIntencionId;

    @Column(name = "quota_number")
    private Integer quotaNumber;

    @Column(name = "quota_value")
    private Double quotaValue;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "capital_balance")
    private Double capitalBalance;

    @Column(name = "investment_value")
    private Double investmentValue;

    @Column(name = "interest_value")
    private Double interestValue;

    @Column(name = "life_insurance")
    private Double lifeInsurance;

    @Column(name = "portfolio_insurance")
    private Double portfolioInsurance;

    @Column(name = "liquidated")
    private String liquidated;
}
