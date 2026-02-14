package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingQuotaForAgreementDto {
    
    private Long quotaId;
    private Integer quotaNumber;
    private String expirationDate;
    private BigDecimal quotaValue;
    private BigDecimal remainingBalance;
    private BigDecimal delayPenalty;
    private Integer daysOverdue;
    private BigDecimal pastduePeriods;
    private boolean isOverdue;
}