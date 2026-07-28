package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Agregado de un cliente para una fecha (o un día dentro de un rango).
 * Mismo shape que ZoneAggregateView, pero la clave de agrupación es
 * person_id en vez de zona_id.
 */
public interface ClientAggregateView {
    LocalDate getSnapshotDate();
    Long getPersonId();
    String getClienteFullname();
    String getClienteDocumento();
    String getZonaNombres();

    Long getTotalCreditos();
    Long getCreditosActivos();
    Long getCreditosCancelados();
    Long getCreditosInactivos();
    Long getCreditosEnMora();
    Long getCreditosAlDia();

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

    BigDecimal getOtrosConceptosGenerado();
    BigDecimal getTotalPagado();
    BigDecimal getSaldoTotal();

    BigDecimal getCuotasPlaneadas();
    BigDecimal getTotalCuotas();
    BigDecimal getCuotasPagadas();
    BigDecimal getCuotasPendientes();

    Integer getDiasMoraMaximo();
    BigDecimal getDiasMoraPromedio();
}