package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comparación entre el primer y el último día del rango consultado para
 * un cliente. Versión simplificada de ResumenEvolucionDto (de zona): no
 * incluye transiciones a nivel de crédito individual (entradas/salidas de
 * mora por crédito, cambios de calificación) porque para un solo cliente
 * esa granularidad no aporta valor adicional sobre lo que ya muestra
 * la serie diaria completa.
 */
@Getter
@Builder
public class ResumenEvolucionClienteDto {
    private LocalDate fechaInicial;
    private LocalDate fechaFinal;
    private BigDecimal saldoInicial;
    private BigDecimal saldoFinal;
    private BigDecimal variacionSaldo;
    private BigDecimal pctVariacionSaldo;
    private BigDecimal capitalPendienteInicial;
    private BigDecimal capitalPendienteFinal;
    private BigDecimal variacionCapitalPendiente;
    private BigDecimal moraPendienteInicial;
    private BigDecimal moraPendienteFinal;
    private BigDecimal variacionMoraPendiente;
    private BigDecimal totalRecuperado;
    private BigDecimal capitalRecuperado;
    private Long creditosEnMoraInicial;
    private Long creditosEnMoraFinal;
    private Long variacionCreditosEnMora;
}