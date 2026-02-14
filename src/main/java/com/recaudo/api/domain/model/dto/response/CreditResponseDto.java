package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponseDto {
    private Long id;
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
   // private String createdAt;
    //private String editedAt;
}