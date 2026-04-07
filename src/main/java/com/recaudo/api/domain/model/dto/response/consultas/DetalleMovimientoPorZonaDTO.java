package com.recaudo.api.domain.model.dto.response.consultas;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleMovimientoPorZonaDTO {

    String zona;
    String concept_key;
    String concept;
    String payment_type;
    BigDecimal value_paid;
    BigDecimal investment_value;
    BigDecimal interest_value;
    BigDecimal life_insurance;
    BigDecimal portfolio_insurance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime date;
    String user;

}
