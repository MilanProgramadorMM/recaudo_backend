package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Análisis histórico de la evolución de la cartera de una zona en un rango de fechas.
 * Respuesta del endpoint 3. Estructura pensada para dashboards:
 *  - resumenEvolucion: KPIs comparativos y totales del período (tarjetas / indicadores).
 *  - serie: un punto por snapshot (gráficas de línea).
 *  - transiciones: cambios diarios de la cartera (gráficas de barras).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneHistoryAnalysisDto {

    private Long zonaId;
    private String zonaNombre;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer diasConSnapshot;

    private ResumenEvolucionDto resumenEvolucion;
    private List<PuntoHistoricoDto> serie;
    private List<TransicionDiariaDto> transiciones;
}
