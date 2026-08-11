package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TodayCollectionProjection {
    Long getCreditId();
    Long getCuotaId();
    Integer getQuotaNumber();
    LocalDate getExpirationDate();
    String getClientName();
    Integer getClientOrden();
    BigDecimal getValorCuota();
    String getZonaCode();
    String getZona();
    Integer getPaidToday();
    LocalDate getPaymentPromiseDate();
    Integer getNoPago();
    String getNoPagoReason();
    String getNombreDia();
}