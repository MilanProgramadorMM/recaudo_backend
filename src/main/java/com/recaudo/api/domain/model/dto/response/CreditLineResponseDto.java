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
public class CreditLineResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal minQuota;
    private BigDecimal maxQuota;
    private Integer minPeriod;
    private Integer maxPeriod;
    private Long taxType;
    private String taxTypeName;
    private Long amortizationType;
    private String amortizationTypeName;
    private String procedureName;
    private boolean lifeInsurance;
    private boolean portfolioInsurance;
    private boolean requireDocumentation;
}
