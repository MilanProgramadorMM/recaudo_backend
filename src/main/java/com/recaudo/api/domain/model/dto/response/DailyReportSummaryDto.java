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
public class DailyReportSummaryDto {
    private Long zonaId;
    private String zonaName;
    private Integer clientesEnLista;
    private Integer clientesVisitados;
    private Integer clientesPagaron;
    private Integer clientesNoPagaron;
    private Integer clientesPromesa;
    private Integer clientesPendientes;
    private BigDecimal totalRecaudado;
}

