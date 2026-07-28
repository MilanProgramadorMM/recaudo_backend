package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ClientCreditView {
    Integer getCreditId();
    String getEstadoCredito();
    LocalDateTime getFechaCreacion();
    LocalDateTime getFechaUltimaEdicion();
    String getLineaCredito();
    String getZona();
    BigDecimal getTotalFinanciado();
    BigDecimal getValorCuota();
    Integer getCuotasPlaneadas();
}