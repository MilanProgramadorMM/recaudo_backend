package com.recaudo.api.domain.usecase;

import com.recaudo.api.domain.gateway.PortfolioSnapshotIGateway;
import com.recaudo.api.domain.model.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class PortfolioSnapshotUseCase {

    @Autowired
    private PortfolioSnapshotIGateway portfolioSnapshotGateway;

    public List<PortfolioSnapshotResponseDto> getBySnapshotDate(LocalDate fecha) {
        return portfolioSnapshotGateway.getBySnapshotDate(fecha);
    }

    public ZoneSnapshotStateDto getZoneState(Long zoneId, LocalDate fecha) {
        return portfolioSnapshotGateway.getZoneState(zoneId, fecha);
    }

    public List<ZoneSnapshotStateDto> getZonesState(List<Long> zoneIds, LocalDate fecha) {
        return portfolioSnapshotGateway.getZonesState(zoneIds, fecha);
    }

    public ZoneHistoryAnalysisDto getZoneHistory(Long zoneId, LocalDate startDate, LocalDate endDate) {
        return portfolioSnapshotGateway.getZoneHistory(zoneId, startDate, endDate);
    }

    public List<ClientListItemDto> getClientsByDate(LocalDate fecha, String busqueda) {
        return portfolioSnapshotGateway.getClientsByDate(fecha, busqueda);
    }

    public ClientPortfolioStateDto getClientState(Long personId, LocalDate fecha) {
        return portfolioSnapshotGateway.getClientState(personId, fecha);
    }

    public ClientHistoryAnalysisDto getClientHistory(Long personId, LocalDate startDate, LocalDate endDate) {
        return portfolioSnapshotGateway.getClientHistory(personId, startDate, endDate);
    }
}
