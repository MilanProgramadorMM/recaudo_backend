package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Bloque reutilizable: montos de un concepto financiero (capital, interés, seguros, mora).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptoMontoDto {
    private BigDecimal generado;
    private BigDecimal pagado;
    private BigDecimal pendiente;
}
