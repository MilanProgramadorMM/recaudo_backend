package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.consultas.DebidoCobrarDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DefaultConsultasDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleCreditosPorZona;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleMovimientoPorZonaDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleSaldoVencidoDTO;
import com.recaudo.api.domain.model.dto.response.consultas.MovimientoPorZonaDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ConsultasGateway {

    List<MovimientoPorZonaDTO> getMovimientosPorZona(Long concept, Long paymentType, LocalDate startDate, LocalDate endDate);

    List<DetalleMovimientoPorZonaDTO> getMovimientosPorZonaDetalle(Long concept, Long paymentType, LocalDate startDate, LocalDate endDate, Long zona);

    List<DefaultConsultasDTO> getSaldoVencidoPorZona(LocalDate startDate, LocalDate endDate);

    List<DetalleSaldoVencidoDTO> getDetalleSaldoVencidoPorZona(LocalDate startDate, LocalDate endDate, Long zona);

    List<DefaultConsultasDTO> getCreditosPorZona(LocalDate startDate, LocalDate endDate);

    List<DetalleCreditosPorZona> getDetalleCreditosPorZona(LocalDate startDate, LocalDate endDate, Long zona);

    List<DebidoCobrarDTO> getDebidoCobrarPorZona(LocalDate startDate, LocalDate endDate);

}
