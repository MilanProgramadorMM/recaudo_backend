package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.DashboardCardGateway;
import com.recaudo.api.domain.model.dto.response.DashboardHistorialDto;
import com.recaudo.api.domain.model.dto.response.DashboardNoPagoSummaryDto;
import com.recaudo.api.domain.model.dto.response.DetalleDebidoCobrarDTO;
import com.recaudo.api.infrastructure.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DashboardCardAdapter implements DashboardCardGateway {

    @Autowired
    private DashboardMetricsRepository repository;

    @Autowired
    private DashboardDebidoCobrarRepository debidoCobrarRepository;

    /*@Override
    public BigDecimal getTotalCartera(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalRecaudado(fechaInicio,fechaFin,zonaId);
    }
     */

    @Override
    public BigDecimal getTotalDebidoCobrar(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long zonaId
    ) {
        // Busca el registro precalculado del job para el día de fechaInicio
        LocalDateTime inicioDia = fechaInicio.toLocalDate().atStartOfDay();
        LocalDateTime finDia    = inicioDia.plusDays(1);

        return debidoCobrarRepository
                .findLatestValueByZonaIdAndFecha(zonaId, inicioDia, finDia)
                .orElseGet(() -> {
                    // Fallback: si el job aún no corrió, calcula en tiempo real
                    log.warn("[DashboardCardAdapter] Sin registro precalculado para " +
                            "zona {} en {}. Ejecutando consulta en tiempo real.", zonaId, inicioDia.toLocalDate());
                    return repository.getTotalDebidoCobrar(fechaInicio, fechaFin, zonaId);
                });
    }

    @Override
    public DashboardNoPagoSummaryDto getTotalNoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalNoPago(fechaInicio, fechaFin, zonaId);
    }

    @Override
    public BigDecimal getTotalRecaudadoZona(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalRecaudado(fechaInicio,fechaFin,zonaId);
    }

    ////////////////////GRAFICOS/////////////////
    @Override
    public List<DashboardHistorialDto> getHistorialDebidoCobrar(
            LocalDate inicio, LocalDate fin, Long zonaId) {
        return repository.getHistorialDebidoCobrar(inicio, fin, zonaId);
    }

    @Override
    public List<DashboardHistorialDto> getHistorialRecaudado(
            LocalDate inicio, LocalDate fin, Long zonaId) {
        return repository.getHistorialRecaudado(inicio, fin, zonaId);
    }

    @Override
    public List<DashboardHistorialDto> getHistorialNoPago(
            LocalDate inicio, LocalDate fin, Long zonaId) {
        return repository.getHistorialNoPago(inicio, fin, zonaId);
    }


    /////////////////////COMPARATIVO VALOR CUOTA////////////////////////
    @Override
    public List<DetalleDebidoCobrarDTO> getDetalleDebidoCobrar(
            LocalDate inicio, LocalDate fin, Long zonaId) {
        return repository.getDetalleDebidoCobrarPorZona(inicio, fin, zonaId);
    }
}
