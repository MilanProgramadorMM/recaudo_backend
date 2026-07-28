package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bloque reutilizable: resumen de cuotas agregadas de una zona.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotasResumenDto {
    private Long planeadas;
    private Long totales;
    private Long pagadas;
    private Long pendientes;
}
