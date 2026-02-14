package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRegisterDto {
    private Long creditIntentionId;
    private Long personId;
    private Long creditLineId;
    private BigDecimal quotaValue;
    private Long periodId;
    private Integer periodQuantity;
    private Long taxTypeId;
    private BigDecimal taxValue;
    private BigDecimal totalIntentionValue;
    private BigDecimal totalInterestValue;
    private BigDecimal totalCapitalValue;
    private BigDecimal itemValue;
    private BigDecimal initialValuePayment;
    private BigDecimal totalFinancedValue;
    private BigDecimal stationery;
}