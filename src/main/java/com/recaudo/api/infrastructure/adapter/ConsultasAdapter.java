package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.ConsultasGateway;
import com.recaudo.api.domain.model.dto.response.consultas.DebidoCobrarDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DefaultConsultasDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleCreditosPorZona;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleMovimientoPorZonaDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleSaldoVencidoDTO;
import com.recaudo.api.domain.model.dto.response.consultas.MovimientoPorZonaDTO;
import com.recaudo.api.infrastructure.helper.util.Utils;
import com.recaudo.api.infrastructure.repository.ConsultasRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class ConsultasAdapter implements ConsultasGateway {

    private ConsultasRepository consultasRepository;

    @Override
    public List<MovimientoPorZonaDTO> getMovimientosPorZona(Long concept, Long paymentType, LocalDate startDate, LocalDate endDate) {
        return consultasRepository.getMovimientosPorZona(concept, paymentType, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @Override
    public List<DetalleMovimientoPorZonaDTO> getMovimientosPorZonaDetalle(Long concept, Long paymentType, LocalDate startDate, LocalDate endDate, Long zona) {
        return consultasRepository.getDetalleMovimientosPorZona(concept, paymentType, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), zona);
    }

    @Override
    public List<DefaultConsultasDTO> getSaldoVencidoPorZona(LocalDate startDate, LocalDate endDate) {
        return consultasRepository.getSaldoVencidoPorZona(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @Override
    public List<DetalleSaldoVencidoDTO> getDetalleSaldoVencidoPorZona(LocalDate startDate, LocalDate endDate, Long zona) {
        return consultasRepository.getDetalleSaldoVencidoPorZona(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), zona);
    }

    @Override
    public List<DefaultConsultasDTO> getCreditosPorZona(LocalDate startDate, LocalDate endDate) {
        return consultasRepository.getCreditosPorZona(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @Override
    public List<DetalleCreditosPorZona> getDetalleCreditosPorZona(LocalDate startDate, LocalDate endDate, Long zona) {
        return consultasRepository.getDetalleCreditosPorZona(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), zona);
    }

    @Override
    public List<DebidoCobrarDTO> getDebidoCobrarPorZona(LocalDate startDate, LocalDate endDate) {
        return consultasRepository.getDebidoCobrarPorZona(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }
}
