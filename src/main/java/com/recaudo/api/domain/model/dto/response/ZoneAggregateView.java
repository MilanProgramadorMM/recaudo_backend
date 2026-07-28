package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection nativa con los KPIs agregados de una zona.
 * Reutilizada tanto para el estado en una fecha (GROUP BY zona)
 * como para la serie histórica diaria (GROUP BY snapshot_date, zona).
 */
public interface ZoneAggregateView {

    LocalDate getSnapshotDate();
    Long getZonaId();
    String getZonaNombre();

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

    BigDecimal getDiasMoraPromedio();
}
