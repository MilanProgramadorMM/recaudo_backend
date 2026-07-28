package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Estado agregado de TODOS los créditos de un cliente en una fecha puntual.
 * Mismo shape que ZoneSnapshotStateDto, pero agrupado por person_id en vez
 * de zona_id — reutiliza los mismos sub-DTOs (ConceptoMontoDto,
 * ConteosCreditoDto, CuotasResumenDto) para mantener consistencia.
 */
@Getter
@Builder
public class ClientPortfolioStateDto {
    private LocalDate fecha;
    private Long personId;
    private String clienteFullname;
    private String clienteDocumento;
    private List<String> zonas;

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

    private Integer diasMoraMaximo;
    private BigDecimal diasMoraPromedio;
}