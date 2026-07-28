package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ClientHistoryAnalysisDto {
    private Long personId;
    private String clienteFullname;
    private LocalDate startDate;
    private LocalDate endDate;
    private int diasConSnapshot;
    private ResumenEvolucionClienteDto resumenEvolucion;
    private List<PuntoHistoricoClienteDto> serie;
}