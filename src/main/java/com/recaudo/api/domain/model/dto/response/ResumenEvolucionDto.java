package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resumen de la evolución de la zona en todo el rango consultado:
 * compara el primer snapshot contra el último y totaliza los cambios del período.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenEvolucionDto {

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    // Saldo total
    private BigDecimal saldoInicial;
    private BigDecimal saldoFinal;
    private BigDecimal variacionSaldo;
    private BigDecimal pctVariacionSaldo;

    // Capital pendiente
    private BigDecimal capitalPendienteInicial;
    private BigDecimal capitalPendienteFinal;
    private BigDecimal variacionCapitalPendiente;

    // Mora pendiente
    private BigDecimal moraPendienteInicial;
    private BigDecimal moraPendienteFinal;
    private BigDecimal variacionMoraPendiente;
    private BigDecimal pctVariacionMoraPendiente;

    // Recuperación (deltas de acumulados en el período)
    private BigDecimal totalRecuperado;
    private BigDecimal capitalRecuperado;

    // Créditos en mora
    private Long creditosEnMoraInicial;
    private Long creditosEnMoraFinal;
    private Long variacionCreditosEnMora;

    // Totales de transiciones del período
    private Long totalIngresaronMora;
    private Long totalSalieronMora;
    private Long totalCambiaronEstado;
    private Long totalCancelados;
    private Long totalCambiosCalificacion;
    private Long totalNuevosCreditos;
    private Long totalCreditosQueSalieron;
}
