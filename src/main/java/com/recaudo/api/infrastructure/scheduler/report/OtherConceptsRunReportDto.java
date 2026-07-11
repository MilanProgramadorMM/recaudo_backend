package com.recaudo.api.infrastructure.scheduler.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reporte completo de la corrida de conceptos adicionales
 * (credit_other_concepts / credit_other_concepts_detail).
 */
@Data
@Builder
public class OtherConceptsRunReportDto {

    private LocalDate fecha;
    private int calculadoresActivos;

    @Builder.Default
    private List<ConceptCalculatorReportDto> calculadores = new ArrayList<>();

    public int totalInserts() {
        return calculadores.stream().mapToInt(c -> c.getInserts().size()).sum();
    }

    public int totalRegistradas() {
        return calculadores.stream().mapToInt(ConceptCalculatorReportDto::getRegistradas).sum();
    }

    public int totalOmitidas() {
        return calculadores.stream().mapToInt(ConceptCalculatorReportDto::getOmitidas).sum();
    }

    public int totalErrores() {
        return calculadores.stream().mapToInt(ConceptCalculatorReportDto::getErrores).sum();
    }

    /**
     * Construye un texto descriptivo, listando todos los INSERT ejecutados.
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  CONCEPTOS ADICIONALES — fecha ").append(fecha).append("\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("Calculadores activos : ").append(calculadoresActivos).append("\n");
        sb.append("Total registradas    : ").append(totalRegistradas()).append("\n");
        sb.append("Total omitidas       : ").append(totalOmitidas()).append("\n");
        sb.append("Total errores        : ").append(totalErrores()).append("\n");
        sb.append("Total INSERTs         : ").append(totalInserts()).append("\n");

        for (ConceptCalculatorReportDto c : calculadores) {
            sb.append("---------------------------------------------------------------\n");
            sb.append("• ").append(c.getLabel())
              .append(" (glotype=").append(c.getGlotypeCode())
              .append("/id=").append(c.getGlotypeId()).append(")\n");
            sb.append("    cuotas elegibles=").append(c.getCuotasElegibles())
              .append("  registradas=").append(c.getRegistradas())
              .append("  omitidas=").append(c.getOmitidas())
              .append("  errores=").append(c.getErrores()).append("\n");

            if (c.getInserts().isEmpty()) {
                sb.append("    (sin INSERTs)\n");
            } else {
                for (InsertLogDto ins : c.getInserts()) {
                    sb.append("    INSERT ").append(ins.getTabla())
                      .append(" → ").append(ins.getDescripcion()).append("\n");
                }
            }
        }
        sb.append("═══════════════════════════════════════════════════════════════");
        return sb.toString();
    }
}
