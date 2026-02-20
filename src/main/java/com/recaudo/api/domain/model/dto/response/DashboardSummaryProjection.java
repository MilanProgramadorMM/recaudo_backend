package com.recaudo.api.domain.model.dto.response;

public interface DashboardSummaryProjection {
    Long getZonaId();
    String getZonaNombre();
    Double getTotalDebidoCobrar();
    Double getTotalRecaudado();
    Double getTotalNoPagado();
}