package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.DashboardCardGateway;
import com.recaudo.api.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DashboardCardAdapter implements DashboardCardGateway {

    @Autowired
    private DashboardMetricsRepository repository;

    /*@Override
    public BigDecimal getTotalCartera(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalRecaudado(fechaInicio,fechaFin,zonaId);
    }
     */

    @Override
    public BigDecimal getTotalDebidoCobrar(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId) {
        return repository.getTotalDebidoCobrar(fechaInicio,fechaFin,zonaId);
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
