package com.recaudo.api.domain.model.dto.response;

import java.time.LocalDate;

/**
 * Projection nativa ligera (estado por crédito y por día) usada para
 * el análisis de transiciones entre snapshots consecutivos de una zona.
 */
public interface CreditTransitionView {
    LocalDate getSnapshotDate();
    Integer getCreditId();
    String getEstadoCredito();
    Integer getDiasMora();
    String getRatingValue();
}
