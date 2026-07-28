package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;

public interface CreditSnapshotProjection {
    Integer getCreditId();
    Integer getPersonId();
    String getCliente();
    String getDocumento();
    Long getZonaId();
    String getZona();
    String getEstadoCredito();

    Long getCreditLineId();
    String getCreditLineNombre();
    Long getPeriodId();
    String getPeriodNombre();
    String getPeriodCodigo();
    Long getTaxTypeId();
    String getTaxTypeNombre();
    BigDecimal getTaxValue();

    Integer getCuotasPlaneadas();
    Integer getTotalCuotas();
    Integer getCuotasPagadas();
    Integer getCuotasPendientes();

    BigDecimal getCapitalGenerado();
    BigDecimal getCapitalPagado();
    BigDecimal getCapitalPendiente();

    BigDecimal getInteresGenerado();
    BigDecimal getInteresPagado();
    BigDecimal getInteresPendiente();

    BigDecimal getSeguroVidaGenerado();
    BigDecimal getSeguroVidaPagado();
    BigDecimal getSeguroVidaPendiente();

    BigDecimal getSeguroCarteraGenerado();
    BigDecimal getSeguroCarteraPagado();
    BigDecimal getSeguroCarteraPendiente();

    BigDecimal getMoraGenerada();
    BigDecimal getMoraPagada();
    BigDecimal getMoraPendiente();

    BigDecimal getTotalPagado();
    BigDecimal getSaldoTotal();
    BigDecimal getOtrosConceptosGenerado();

    Integer getDiasMora();
}