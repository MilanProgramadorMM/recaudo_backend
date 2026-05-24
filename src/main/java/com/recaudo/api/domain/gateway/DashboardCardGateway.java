package com.recaudo.api.domain.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DashboardCardGateway {

    //BigDecimal getTotalCartera(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalDebidoCobrar(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalNoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);
    BigDecimal getTotalRecaudadoZona(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long zonaId);

}
