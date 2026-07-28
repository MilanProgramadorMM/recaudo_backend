package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bloque reutilizable: conteos de créditos de una zona por estado y situación de mora.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteosCreditoDto {
    private Long total;
    private Long activos;
    private Long cancelados;
    private Long inactivos;
    private Long enMora;
    private Long alDia;
}
