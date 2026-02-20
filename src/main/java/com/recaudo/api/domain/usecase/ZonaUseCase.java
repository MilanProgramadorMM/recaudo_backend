package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ZonaGateway;
import com.recaudo.api.domain.model.dto.response.DailyReportDetailDto;
import com.recaudo.api.domain.model.dto.response.DailyReportSummaryDto;
import com.recaudo.api.domain.model.dto.response.DashboardSummaryProjection;
import com.recaudo.api.domain.model.dto.response.ZonaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;
import com.recaudo.api.domain.model.entity.ZonaEntity;
import com.recaudo.api.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@UseCase
@AllArgsConstructor
public class ZonaUseCase {

    private ZonaGateway gateway;


    public List<ZonaResponseDto> getStatusTrue() {
        return gateway.getStatusTrue();
    }

    public List<DashboardSummaryProjection> getDashboardSummary(LocalDate fechaInicio, LocalDate fechaFin, Long zonaId) {
        if (zonaId != null) {
            Optional<ZonaEntity> existzona = gateway.getById(zonaId);
            if (!existzona.isPresent()) {
                throw new ResourceNotFoundException("No existe la zona con ID: " + zonaId);
            }
        }

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = (fechaInicio != null) ? fechaInicio : hoy;
        LocalDate fin = (fechaFin != null) ? fechaFin : hoy;

        return gateway.getDashboardSummary(inicio, fin, zonaId);
    }


        public List<ZonaResponseDto> getAll() {
        return gateway.getAll();
    }

    public ZonaResponseDto create(ZonaCreateDto zonaCreateDto) {
        return gateway.create(zonaCreateDto);
    }

    public ZonaResponseDto update(Long id, ZonaCreateDto zonaCreateDto) {
        return gateway.update(id, zonaCreateDto);
    }

    public void delete(Long id) {
        gateway.delete(id);
    }

    public Optional<DailyReportSummaryDto> getDailySummaryByZone(String zonaName, LocalDate fecha){
        return gateway.getDailySummaryByZone(zonaName, fecha);
    }

    public List<DailyReportDetailDto> getDailyDetailByZone(String username, LocalDate fech){
        return gateway.getDailyDetailByZone(username, fech);
    }


}
