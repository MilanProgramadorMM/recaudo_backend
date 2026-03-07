package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRecaudoStatusDto {
    
    // Información del crédito
    private Long creditId;
    private Long personId;
    private BigDecimal quotaValue;
    private Integer periodQuantity;
    private String periodName;
    private BigDecimal totalIntentionValue;
    private BigDecimal totalInterestValue;
    private BigDecimal totalCapitalValue;
    private BigDecimal taxValue;
    private BigDecimal stationery;

    // Resumen de cuotas
    private Integer totalCuotas;
    private Integer cuotasPagadas;
    private Integer cuotasPendientes;
    
    // Resumen financiero
    private BigDecimal totalPagado;
    private BigDecimal totalPendiente;
    private BigDecimal porcentajePagado;
    
    // Detalle de cuotas
    private List<QuotaDetailDto> cuotas;
    
    // Historial de recaudos
    private List<RecaudoDetailDto> recaudos;
}
