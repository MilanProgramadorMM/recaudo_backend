package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmortizationRequestDto {
    private String code;
    private double capital;
    private double interes;
    private int periodos;
    private int conversionFactor;
}
