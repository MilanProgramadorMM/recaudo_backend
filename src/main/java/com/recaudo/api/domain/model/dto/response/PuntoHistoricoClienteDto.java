package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Un punto (un día) de la serie histórica de un cliente. */
@Getter
@Builder
public class PuntoHistoricoClienteDto {
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
    private Integer diasMoraMaximo;
    private BigDecimal diasMoraPromedio;
}