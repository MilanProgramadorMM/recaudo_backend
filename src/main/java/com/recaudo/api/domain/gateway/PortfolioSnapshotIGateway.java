package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotIGateway {

    List<PortfolioSnapshotResponseDto> getBySnapshotDate(LocalDate fecha);

    ZoneSnapshotStateDto getZoneState(Long zoneId, LocalDate fecha);

    List<ZoneSnapshotStateDto> getZonesState(List<Long> zoneIds, LocalDate fecha);

    ZoneHistoryAnalysisDto getZoneHistory(Long zoneId, LocalDate startDate, LocalDate endDate);

    List<ClientListItemDto> getClientsByDate(LocalDate fecha, String busqueda);

    ClientPortfolioStateDto getClientState(Long personId, LocalDate fecha);

    ClientHistoryAnalysisDto getClientHistory(Long personId, LocalDate startDate, LocalDate endDate);

}
