package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Punto de la serie temporal: estado agregado de la zona en un snapshot (un día).
 * Pensado para alimentar directamente gráficas de línea en el frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoHistoricoDto {

    private LocalDate fecha;

    private Long totalCreditos;
    private Long creditosActivos;
    private Long creditosEnMora;
    private Long creditosCancelados;

    private BigDecimal saldoTotal;
    private BigDecimal capitalPendiente;
    private BigDecimal interesPendiente;
    private BigDecimal moraPendiente;

    private BigDecimal capitalPagado;
    private BigDecimal totalPagado;

    private Long cuotasPagadas;
    private Long cuotasPendientes;

    private BigDecimal diasMoraPromedio;
}
