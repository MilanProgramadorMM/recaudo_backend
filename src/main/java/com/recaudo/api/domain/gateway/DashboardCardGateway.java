package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.DashboardHistorialDto;
import com.recaudo.api.domain.model.dto.response.DashboardNoPagoSummaryDto;
import com.recaudo.api.domain.model.dto.response.DetalleDebidoCobrarDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DashboardSummaryDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardCardGateway {

    ////////////////////CARD/////////////////
    //BigDecimal getTotalCartera(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalDebidoCobrarValorCuota(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    DashboardNoPagoSummaryDto getTotalNoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalRecaudadoZona(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    DashboardSummaryDto getDashboardSummary(LocalDate fecha, Long zonaId);


    ////////////////////GRAFICOS/////////////////
    // DashboardCardGateway.java — agrega estos métodos
    List<DashboardHistorialDto> getHistorialDebidoCobrar(LocalDate inicio, LocalDate fin, Long zonaId);
    List<DashboardHistorialDto> getHistorialRecaudado(LocalDate inicio, LocalDate fin, Long zonaId);
    List<DashboardHistorialDto> getHistorialNoPago(LocalDate inicio, LocalDate fin, Long zonaId);


    /////////VALOR CUOTA COMPARARTIVO GRAFICO///////////////
    List<DetalleDebidoCobrarDTO> getDetalleDebidoCobrar(LocalDate inicio, LocalDate fin, Long zonaId);

}
