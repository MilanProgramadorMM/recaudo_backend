package com.recaudo.api.domain.model.dto.response;


public interface DailyCollectionProjection {

    Long getCreditId();

    Long getCuotaId();

    Integer getQuotaNumber();

    String getExpirationDate();

    String getClientName();

    Integer  getPaidToday();
    String getPaidFull();
    String getLiquidated();

    String getPaymentPromiseDate();
    Integer getNoPago();
    String getNoPagoReason();
    Double getClientCuota();

    Integer getClientOrden();

    String getZona();


}
