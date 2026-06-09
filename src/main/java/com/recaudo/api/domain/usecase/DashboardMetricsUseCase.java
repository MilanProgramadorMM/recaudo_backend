package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.DashboardCardGateway;
import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.response.consultas.DashboardSummaryDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;
import com.recaudo.api.domain.model.entity.ZonaEntity;
import com.recaudo.api.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@UseCase
@AllArgsConstructor
public class DashboardMetricsUseCase {

    private DashboardCardGateway gateway;

    public DashboardSummaryDto getDashboardSummary(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long zonaId
    ) {

        validateDates(fechaInicio, fechaFin);

        LocalDateTime inicio =
                fechaInicio.atStartOfDay();

        LocalDateTime fin =
                fechaFin.plusDays(1).atStartOfDay();

        BigDecimal debidoCobrar =
                gateway.getTotalDebidoCobrar(
                        inicio,
                        fin,
                        zonaId
                );

        BigDecimal recaudado =
                gateway.getTotalRecaudadoZona(
                        inicio,
                        fin,
                        zonaId
                ).abs();

        DashboardNoPagoSummaryDto noPagadoSummary =
                gateway.getTotalNoPago(
                        inicio,
                        fin,
                        zonaId
                );

        BigDecimal cartera =
                debidoCobrar.subtract(recaudado);

        return DashboardSummaryDto.builder()
                .totalDebidoCobrar(debidoCobrar)
                .totalRecaudado(recaudado)
                .totalNoPagado(noPagadoSummary.totalValue())
                .totalNoPagoCantidad(noPagadoSummary.totalCantidad())
                .totalCartera(cartera)
                .build();
    }

    private void validateDates(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (fechaInicio == null) {
            throw new IllegalArgumentException(
                    "La fecha inicio es obligatoria"
            );
        }

        if (fechaFin == null) {
            throw new IllegalArgumentException(
                    "La fecha fin es obligatoria"
            );
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha fin no puede ser menor que la fecha inicio"
            );
        }

        long days = ChronoUnit.DAYS.between(
                fechaInicio,
                fechaFin
        );

        // Recomendado para proteger la DB
        if (days > 31) {
            throw new IllegalArgumentException(
                    "El rango máximo permitido es de 31 días"
            );
        }
    }

}
