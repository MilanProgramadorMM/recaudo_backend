package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;

public interface CreditCausadoProjection {
    Long getId();
    BigDecimal getTotalCapitalValue();
    String getCreatedAt();
    String getClientName();
}