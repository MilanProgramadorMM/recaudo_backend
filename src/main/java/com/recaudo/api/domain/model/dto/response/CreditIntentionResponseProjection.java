package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;

public interface CreditIntentionResponseProjection {
    Long getIntentionId();
    String getClientName();
    String getDocument();
    BigDecimal getTotalCapitalValue();
    BigDecimal getTotalIntentionValue();
    BigDecimal getQuotaValue();
    Long getPeriodQuantity();
    String getZona();
    String getCreatedAt();
}