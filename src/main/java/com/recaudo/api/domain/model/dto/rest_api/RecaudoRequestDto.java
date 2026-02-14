package com.recaudo.api.domain.model.dto.rest_api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecaudoRequestDto {
    
    @NotNull(message = "El ID del crédito es obligatorio")
    private Long creditId;
    
    @NotNull(message = "El valor pagado es obligatorio")
    @Positive(message = "El valor pagado debe ser positivo")
    private BigDecimal valuePaid;

    @NotNull(message = "El tipo de pago es obligatorio")
    private Long paymentTypeId;

    private Long bankId;

    private String accountNumber;

    @NotNull(message = "El tipo de distribucion es obligatorio")
    private String distributionType;
}