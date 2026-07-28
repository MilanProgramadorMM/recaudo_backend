package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClientCreditDto {
    private Integer creditId;
    private String estadoCredito;
    private LocalDateTime fechaCreacion;

    /**
     * Solo se completa cuando estadoCredito = CANCELLED.
     * Aproximación basada en credit.edited_at (ver nota en el endpoint):
     * es confiable mientras el crédito no se edite por otros motivos
     * después de cancelado.
     */
    private LocalDateTime fechaCancelacion;

    private String lineaCredito;
    private String zona;
    private BigDecimal totalFinanciado;
    private BigDecimal valorCuota;
    private Integer cuotasPlaneadas;
}