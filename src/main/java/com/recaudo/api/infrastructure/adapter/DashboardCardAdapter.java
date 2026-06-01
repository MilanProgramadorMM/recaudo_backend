package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.DashboardCardGateway;
import com.recaudo.api.infrastructure.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    public BigDecimal getTotalNoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalNoPago(fechaInicio,fechaFin,zonaId);
    }

    @Override
    public BigDecimal getTotalRecaudadoZona(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalRecaudado(fechaInicio,fechaFin,zonaId);
    }
}
