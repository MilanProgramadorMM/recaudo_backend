package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConceptComputeResult {

    private BigDecimal value;   // null si se omite
    private String reason;      // motivo cuando se omite; null si se registra

    public static ConceptComputeResult ok(BigDecimal value) {
        return ConceptComputeResult.builder().value(value).build();
    }

    public static ConceptComputeResult omitido(String reason) {
        return ConceptComputeResult.builder().reason(reason).build();
    }

    public boolean isPresent() {
        return value != null;
    }
}