package com.recaudo.api.infrastructure.scheduler.report;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de la ejecución de un único calculador de conceptos
 * (mora, GMF, seguros, etc.) dentro del job.
 */
@Data
@Builder
public class ConceptCalculatorReportDto {

    private String label;
    private String glotypeCode;
    private Long glotypeId;

    private int cuotasElegibles;
    private int registradas;
    private int omitidas;
    private int errores;

    @Builder.Default
    private List<InsertLogDto> inserts = new ArrayList<>();

    public void addInserts(List<InsertLogDto> nuevos) {
        this.inserts.addAll(nuevos);
    }
}
