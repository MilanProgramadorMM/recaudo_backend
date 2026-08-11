package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PortfolioProjection {
    Long getCreditId();
    String getClientName();
    Integer getClientOrden();
    String getZonaCode();
    String getZona();
    BigDecimal getTotalCapitalValue();
    Integer getCuotasPendientes();
    LocalDate getProximaCuotaFecha();
    Integer getProximaCuotaNumero();
    BigDecimal getSaldoPendiente();
    LocalDate getFechaCredito();
    String getLineaname();
    String getPeriodo();
    Integer getPlazoCredito();
    LocalDate getFechaVence();
    Integer getTotalCuotas();
    Integer getCuotasPagadas();
    Integer getCuotasVencidas();
    String getDireccion();
    String getWhatsapp();
    String getCelular();
    String getBarrio();
    String getMunicipio();
    Long getProximaCuotaId();
}