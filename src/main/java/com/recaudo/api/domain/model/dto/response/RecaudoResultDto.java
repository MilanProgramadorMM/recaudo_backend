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
public class RecaudoResultDto {
    private Long creditId;
    private BigDecimal totalPaid;
    private Integer cuotasPagadas;
    private Integer cuotasFaltantes;
    private BigDecimal saldoSobrante;
}