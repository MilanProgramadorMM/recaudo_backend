package com.recaudo.api.domain.model.dto.response.consultas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDto {

    private BigDecimal totalDebidoCobrar;

    private BigDecimal totalValorCuota;

    private BigDecimal totalRecaudado;

    private BigDecimal totalNoPagado;

    private BigDecimal totalCartera;
    private Long totalNoPagoCantidad;
}