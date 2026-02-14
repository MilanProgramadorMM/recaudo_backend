package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmortizationResponseDto {
    private String code;
    private String message;
    private List<AmortizationItemDto> amortizationTable;
    private BigDecimal totalPagar;
}
