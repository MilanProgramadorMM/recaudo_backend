package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Estado agregado (KPIs) de la cartera de una zona en una fecha específica.
 * Respuesta de los endpoints 1 (una zona) y 2 (varias zonas).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneSnapshotStateDto {

    private LocalDate fecha;
    private Long zonaId;
    private String zonaNombre;

    private ConteosCreditoDto conteos;

    private ConceptoMontoDto capital;
    private ConceptoMontoDto interes;
    private ConceptoMontoDto seguroVida;
    private ConceptoMontoDto seguroCartera;
    private ConceptoMontoDto mora;

    private BigDecimal otrosConceptosGenerado;
    private BigDecimal totalPagado;
    private BigDecimal saldoTotal;

    private CuotasResumenDto cuotas;

    private BigDecimal diasMoraPromedio;

    private List<CalificacionBucketDto> distribucionCalificacion;
}
