package com.recaudo.api.domain.model.dto.response.consultas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleCreditosPorZona {

    private Long creditId;
    private Long creditIntentionId;
    private String fullName;
    private String period;
    private Integer periodQuantity;
    private BigDecimal quotaValue;
    private String creditLine;
    private BigDecimal totalCapitalValue;
    private BigDecimal initialValuePayment;
    private BigDecimal totalFinancedValue;
    private BigDecimal totalIntentionValue;
    private BigDecimal totalInterestValue;
    private BigDecimal itemValue;
    private BigDecimal stationery;


}
