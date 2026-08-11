package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OverdueProjection {
    Long getCreditId();
    String getClientName();
    Integer getClientOrden();
    String getZonaCode();
    String getZona();
    BigDecimal getTotalCapitalValue();   // ← AGREGADO
    Integer getPeriodosVencidos();
    LocalDate getPrimeraCuotaVencida();
    Integer getPrimeraCuotaVencidaNumero();
    BigDecimal getTotalMoraCredito();
    BigDecimal getSaldoPendiente();      // ← también recomiendo agregar este, ver abajo
    String getDireccion();
    String getWhatsapp();
    String getCelular();
    String getBarrio();
    String getMunicipio();
    LocalDate getFechaCredito();
    String getLineaname();
    String getPeriodo();
    Integer getPlazoCredito();
    LocalDate getFechaVence();
    Integer getTotalCuotas();
    Integer getCuotasPagadas();
    Integer getCuotasVencidas();
    Long getPrimeraCuotaVencidaId();
}