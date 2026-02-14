package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.DailyReportDetailDto;
import com.recaudo.api.domain.model.dto.response.DailyReportSummaryDto;
import com.recaudo.api.domain.model.dto.response.ZonaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;
import com.recaudo.api.domain.model.entity.ZonaEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ZonaGateway {

    List<ZonaResponseDto> getStatusTrue();
    List<ZonaResponseDto> getAll();
    ZonaResponseDto create(ZonaCreateDto departamentoCreateDto);
    ZonaResponseDto update(Long id, ZonaCreateDto departamentoCreateDto);
    Optional<ZonaEntity> getById(Long id);
    void delete(Long id);
    List<DailyReportDetailDto> getDailyDetailByZone(String username, LocalDate fech);
    Optional<DailyReportSummaryDto> getDailySummaryByZone(String zonaName, LocalDate fecha);
}
