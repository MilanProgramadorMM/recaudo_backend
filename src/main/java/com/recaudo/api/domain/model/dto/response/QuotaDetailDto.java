package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaDetailDto {
    private Long quotaId;
    private Integer quotaNumber;
    private String expirationDate;
    private String liquidated;
    private String paidFull;
    private BigDecimal quotaValue;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    
    // Saldos pendientes
    private BigDecimal portfolioInsurancePending;
    private BigDecimal lifeInsurancePending;
    private BigDecimal interestPending;
    private BigDecimal investmentPending;
    private BigDecimal totalPending;
    
    // Información de estado
    private Boolean isPaid;
    private Boolean isOverdue;
    private Boolean hasInterestPayment;
    private BigDecimal delayPenalty;
    private Integer daysOverdue;

}