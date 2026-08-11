package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CardDataProjection {
    Long getCreditId();
    String getClientName();
    Integer getClientOrden();
    String getZonaCode();
    String getZona();
    BigDecimal getTotalCapitalValue();
    BigDecimal getSaldoPendiente();
    BigDecimal getTotalMoraCredito();
    Integer getPeriodosVencidos();
    // Específicos de cuota (solo aplican en "cobro hoy")
    Long getCuotaId();
    Integer getQuotaNumber();
    LocalDate getExpirationDate();
    BigDecimal getValorCuota();
    BigDecimal getSaldoPendienteCuota();
    BigDecimal getInterestMora();
    Integer getPaidToday();
    String getPaidFull();
    LocalDate getPaymentPromiseDate();
    Integer getNoPago();
    String getNoPagoReason();
    String getNombreDia();
    // Específicos de cartera
    Integer getCuotasPendientes();
    LocalDate getProximaCuotaFecha();
    Integer getProximaCuotaNumero();
    // Específicos de mora
    LocalDate getPrimeraCuotaVencida();
    Integer getPrimeraCuotaVencidaNumero();
}