package com.recaudo.api.domain.usecase;


import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ConsultasGateway;
import com.recaudo.api.domain.model.dto.response.consultas.DebidoCobrarDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DefaultConsultasDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleCreditosPorZona;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleMovimientoPorZonaDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleSaldoVencidoDTO;
import com.recaudo.api.domain.model.dto.response.consultas.MovimientoPorZonaDTO;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@UseCase
public class ConsultasUseCase {

    private ConsultasGateway consultasGateway;

    public List<MovimientoPorZonaDTO> getMovimientosPorZona(Long concept, Long paymentType, LocalDate startDate, LocalDate endDate) {
        return consultasGateway.getMovimientosPorZona(concept, paymentType, startDate, endDate);
    }

    public List<DetalleMovimientoPorZonaDTO> getDetalleMovimientosPorZona(Long concept, Long paymentType, LocalDate startDate, LocalDate endDate, Long zona) {
        return consultasGateway.getMovimientosPorZonaDetalle(concept, paymentType, startDate, endDate, zona);
    }

    public List<DefaultConsultasDTO> getSaldoVencidoPorZona(LocalDate startDate, LocalDate endDate) {
        return consultasGateway.getSaldoVencidoPorZona(startDate, endDate);
    }

    public List<DetalleSaldoVencidoDTO> getDetalleSaldoVencidoPorZona(LocalDate startDate, LocalDate endDate, Long zona) {
        return consultasGateway.getDetalleSaldoVencidoPorZona(startDate, endDate, zona);
    }

    public List<DefaultConsultasDTO> getCreditosPorZona(LocalDate startDate, LocalDate endDate) {
        return consultasGateway.getCreditosPorZona(startDate, endDate);
    }

    public List<DetalleCreditosPorZona> getDetalleCreditosPorZona(LocalDate startDate, LocalDate endDate, Long zona) {
        return consultasGateway.getDetalleCreditosPorZona(startDate, endDate, zona);
    }

    public List<DebidoCobrarDTO> getDebidoCobrarPorZona(LocalDate startDate, LocalDate endDate) {
        return consultasGateway.getDebidoCobrarPorZona(startDate, endDate);
    }

}
