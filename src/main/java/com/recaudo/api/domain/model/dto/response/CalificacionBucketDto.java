package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bloque reutilizable: cantidad de créditos por calificación (distribución de riesgo).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionBucketDto {
    private String ratingValue;
    private Long cantidad;
}
