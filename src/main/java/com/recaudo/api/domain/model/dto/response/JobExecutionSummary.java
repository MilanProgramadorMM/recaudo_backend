package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class JobExecutionSummary {

    private LocalDate fechaEjecucion;
    private int totalCreditos;
    private int creditosConError;

    @Builder.Default
    private List<CalculatorSummary> resultadosPorCalculador = new ArrayList<>();

    @Builder.Default
    private List<String> erroresCredito = new ArrayList<>();

    @Data
    @Builder
    public static class CalculatorSummary {
        private String label;
        private String glotypeCode;
        private int cuotasEvaluadas;
        private int registradas;
        private int omitidas;
        private int errores;
        private BigDecimal valorTotalGenerado;
        @Builder.Default
        private List<QuotaDetail> detalle = new ArrayList<>();
    }

    @Data
    @Builder
    public static class QuotaDetail {
        private Long creditId;
        private Integer quotaNumber;
        private LocalDate expirationDate;
        private String status;   // REGISTRADA, OMITIDA, ERROR
        private String motivo;   // null si REGISTRADA
        private BigDecimal valor; // null si no REGISTRADA
    }

    public String toLogSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== RESUMEN EJECUCION JOB ==========\n");
        sb.append("Fecha ejecucion   : ").append(fechaEjecucion).append("\n");
        sb.append("Total creditos    : ").append(totalCreditos).append("\n");
        sb.append("Creditos con error: ").append(creditosConError).append("\n");
        sb.append("---------------------------------------------\n");
        for (CalculatorSummary cs : resultadosPorCalculador) {
            sb.append(String.format(
                "[%s] evaluadas=%d | registradas=%d | omitidas=%d | errores=%d | valor_total=%s%n",
                cs.getLabel(), cs.getCuotasEvaluadas(), cs.getRegistradas(),
                cs.getOmitidas(), cs.getErrores(), cs.getValorTotalGenerado()));
        }
        if (!erroresCredito.isEmpty()) {
            sb.append("---------------------------------------------\n");
            sb.append("Errores por credito:\n");
            erroresCredito.forEach(e -> sb.append("  - ").append(e).append("\n"));
        }
        sb.append("==============================================");
        return sb.toString();
    }
}