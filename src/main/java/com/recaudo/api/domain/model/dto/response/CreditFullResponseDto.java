package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CreditFullResponseDto {

    private Long id;
    private Long creditIntentionId;
    private BigDecimal quotaValue;
    private Integer periodQuantity;
    private BigDecimal totalIntentionValue;
    private BigDecimal totalInterestValue;
    private BigDecimal totalCapitalValue;
    private BigDecimal totalFinancedValue;

    private Long zoneId;
    private String zoneName;
    private String document;
    private String fullname;
    private String phoneNumber;

    private Long creditLineId;
    private String creditLineName;

}
