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
public class DetalleSaldoVencidoDTO {

    Long creditId;
    Long creditIntentionId;
    String zona;
    String personName;
    BigDecimal value;
    Integer diasMora;
    Double periodosVencidos;
    BigDecimal interesMoratorio;
    Boolean isOverdue;

}
