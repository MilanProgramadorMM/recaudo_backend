package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.usecase.PortfolioSnapshotUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("portfolio-snapshots")
public class PortfolioSnapshotController {

    @Autowired
    private PortfolioSnapshotUseCase portfolioSnapshotUseCase;

    @GetMapping("/get-by-date/{fecha}")
    public ResponseEntity<DefaultResponseDto<List<PortfolioSnapshotResponseDto>>> getBySnapshotDate(
            @PathVariable("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<PortfolioSnapshotResponseDto> data = portfolioSnapshotUseCase.getBySnapshotDate(fecha);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<PortfolioSnapshotResponseDto>>builder()
                        .message("Snapshots de cartera obtenidos exitosamente")
                        .status(HttpStatus.OK)
                        .details("Snapshots encontrados para la fecha: " + fecha)
                        .data(data)
                        .build()
        );
    }

    // Endpoint 1: estado de la cartera de una zona en una fecha específica
    // GET /portfolio-snapshots/zone/{zoneId}?date=2026-07-10
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<DefaultResponseDto<ZoneSnapshotStateDto>> getZoneState(
            @PathVariable("zoneId") Long zoneId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ZoneSnapshotStateDto data = portfolioSnapshotUseCase.getZoneState(zoneId, date);

        return ResponseEntity.ok(
                DefaultResponseDto.<ZoneSnapshotStateDto>builder()
                        .message("Estado de la cartera de la zona obtenido exitosamente")
                        .status(HttpStatus.OK)
                        .details("Estado de la zona " + zoneId + " para la fecha: " + date)
                        .data(data)
                        .build()
        );
    }

    // Endpoint 2: estado de varias zonas en una fecha específica
    // GET /portfolio-snapshots/zones?zoneIds=1,2,3,4&date=2026-07-10
    @GetMapping("/zones")
    public ResponseEntity<DefaultResponseDto<List<ZoneSnapshotStateDto>>> getZonesState(
            @RequestParam("zoneIds") List<Long> zoneIds,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<ZoneSnapshotStateDto> data = portfolioSnapshotUseCase.getZonesState(zoneIds, date);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<ZoneSnapshotStateDto>>builder()
                        .message("Estado de la cartera de las zonas obtenido exitosamente")
                        .status(HttpStatus.OK)
                        .details("Estado de " + zoneIds.size() + " zona(s) para la fecha: " + date)
                        .data(data)
                        .build()
        );
    }

    // Endpoint 3: evolución histórica de una zona en un rango de fechas
    // GET /portfolio-snapshots/zone/{zoneId}/history?startDate=2026-07-01&endDate=2026-07-31
    @GetMapping("/zone/{zoneId}/history")
    public ResponseEntity<DefaultResponseDto<ZoneHistoryAnalysisDto>> getZoneHistory(
            @PathVariable("zoneId") Long zoneId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ZoneHistoryAnalysisDto data = portfolioSnapshotUseCase.getZoneHistory(zoneId, startDate, endDate);

        return ResponseEntity.ok(
                DefaultResponseDto.<ZoneHistoryAnalysisDto>builder()
                        .message("Evolución histórica de la cartera de la zona obtenida exitosamente")
                        .status(HttpStatus.OK)
                        .details("Zona " + zoneId + " entre " + startDate + " y " + endDate)
                        .data(data)
                        .build()
        );
    }

    // Endpoint: lista/buscador de clientes con cartera en una fecha
    // GET /portfolio-snapshots/clients?date=2026-07-15&search=juan
    @GetMapping("/clients")
    public ResponseEntity<DefaultResponseDto<List<ClientListItemDto>>> getClients(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "search", required = false) String search) {

        List<ClientListItemDto> data = portfolioSnapshotUseCase.getClientsByDate(date, search);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<ClientListItemDto>>builder()
                        .message("Clientes con cartera obtenidos exitosamente")
                        .status(HttpStatus.OK)
                        .details("Clientes encontrados para la fecha: " + date)
                        .data(data)
                        .build()
        );
    }

    // Endpoint: estado de la cartera de un cliente en una fecha específica
// GET /portfolio-snapshots/client/{personId}?date=2026-07-15
    @GetMapping("/client/{personId}")
    public ResponseEntity<DefaultResponseDto<ClientPortfolioStateDto>> getClientState(
            @PathVariable("personId") Long personId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ClientPortfolioStateDto data = portfolioSnapshotUseCase.getClientState(personId, date);

        return ResponseEntity.ok(
                DefaultResponseDto.<ClientPortfolioStateDto>builder()
                        .message("Estado de la cartera del cliente obtenido exitosamente")
                        .status(HttpStatus.OK)
                        .details("Estado del cliente " + personId + " para la fecha: " + date)
                        .data(data)
                        .build()
        );
    }

    // Endpoint: evolución histórica de un cliente en un rango de fechas
// GET /portfolio-snapshots/client/{personId}/history?startDate=2026-07-01&endDate=2026-07-31
    @GetMapping("/client/{personId}/history")
    public ResponseEntity<DefaultResponseDto<ClientHistoryAnalysisDto>> getClientHistory(
            @PathVariable("personId") Long personId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ClientHistoryAnalysisDto data = portfolioSnapshotUseCase.getClientHistory(personId, startDate, endDate);

        return ResponseEntity.ok(
                DefaultResponseDto.<ClientHistoryAnalysisDto>builder()
                        .message("Evolución histórica de la cartera del cliente obtenida exitosamente")
                        .status(HttpStatus.OK)
                        .details("Cliente " + personId + " entre " + startDate + " y " + endDate)
                        .data(data)
                        .build()
        );
    }
}
