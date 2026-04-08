package com.recaudo.api.domain.model.dto.response;


import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyCollectionProjection {
    Long getCreditId();
    Long getCuotaId();
    Integer getQuotaNumber();
    LocalDate getExpirationDate();
    String getClientName();
    Integer getClientOrden();
    BigDecimal getClientCuota();
    String getZona();
    Integer getPaidToday();
    String getPaidFull();
    String getLiquidated();
    LocalDate getPaymentPromiseDate();
    Integer getNoPago();
    String getNoPagoReason();
    String getPeriodo();
    Integer getPlazoCredito();
    BigDecimal getValorCuota();
    Integer getCuotasPagadas();
    Integer getCuotasVencidas();
    BigDecimal getSaldoPendiente();
    Integer getTotalCuotas();
    String getDireccion();
    String getTelefono();
    String getBarrio();
    String getMunicipio();
    String getNombreDia();
}