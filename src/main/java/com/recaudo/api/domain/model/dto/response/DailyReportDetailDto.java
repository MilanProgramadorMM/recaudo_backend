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
public class DailyReportDetailDto {
    private String clientName;
    private Integer clientOrden;
    private Integer quotaNumber;
    private BigDecimal quotaValue;
    private String expirationDate;
    private String estado; // PAGADO, NO_PAGO, PROMESA, SIN_VISITA
    private String motivoNoPago;
    private String observacion;
    private String fechaPromesa;
    private BigDecimal montoRecaudado;
}