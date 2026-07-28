package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PortfolioSnapshotSummary {
    private LocalDate fecha;
    private int totalCreditosProcesados;
    private int paginasConError;
    private String jobExecutionId;
}