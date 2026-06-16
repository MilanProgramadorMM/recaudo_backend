package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.DashboardHistorialDto;
import com.recaudo.api.domain.model.dto.response.DashboardNoPagoSummaryDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardCardGateway {

    ////////////////////CARD/////////////////
    //BigDecimal getTotalCartera(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalDebidoCobrar(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    DashboardNoPagoSummaryDto getTotalNoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalRecaudadoZona(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);

    ////////////////////GRAFICOS/////////////////
    // DashboardCardGateway.java — agrega estos métodos
    List<DashboardHistorialDto> getHistorialDebidoCobrar(LocalDate inicio, LocalDate fin, Long zonaId);
    List<DashboardHistorialDto> getHistorialRecaudado(LocalDate inicio, LocalDate fin, Long zonaId);
    List<DashboardHistorialDto> getHistorialNoPago(LocalDate inicio, LocalDate fin, Long zonaId);
}
