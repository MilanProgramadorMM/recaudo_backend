package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Response detalle
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDetailResponseDto {
    private Long id;
    private Long cuotaId;
    private Integer quotaNumber;
    private Integer daysLate;
    private BigDecimal pastduePeriods;
    private BigDecimal balancePending;
    private BigDecimal delayPenalty;
}