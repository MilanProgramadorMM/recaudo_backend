package com.recaudo.api.domain.model.dto.response.consultas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovimientoPorZonaDTO {

    Long zone_id;
    String zone_name;
    BigDecimal total_recaudado;
    BigDecimal total_capital;
    BigDecimal total_interes;
    BigDecimal total_seguro_vida;
    BigDecimal total_seguro_cartera;

}
