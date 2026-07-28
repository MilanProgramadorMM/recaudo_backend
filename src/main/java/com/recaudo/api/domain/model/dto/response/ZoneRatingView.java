package com.recaudo.api.domain.model.dto.response;

/**
 * Projection nativa con la distribución de créditos por calificación dentro de una zona.
 */
public interface ZoneRatingView {
    Long getZonaId();
    String getRatingValue();
    Long getCantidad();
}
