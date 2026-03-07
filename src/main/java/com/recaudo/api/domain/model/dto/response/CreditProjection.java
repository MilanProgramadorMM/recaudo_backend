package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CreditProjection {
    Long getId();
    Long getCreditIntentionId();
    BigDecimal getQuotaValue();
    Integer getPeriodQuantity();
    BigDecimal getTotalIntentionValue();
    BigDecimal getTotalInterestValue();
    BigDecimal getTotalCapitalValue();
    BigDecimal getTotalFinancedValue();
    Long getCreditLineId();
    String getCreditLineName();
    String getFullname();
    String getDocument();
    String getZoneName();
    String getPeriodName();
    LocalDateTime getCreatedAt();
}