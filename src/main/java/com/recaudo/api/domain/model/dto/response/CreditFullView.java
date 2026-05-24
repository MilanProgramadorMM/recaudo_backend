// CreditFullView.java
package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CreditFullView {
    Long getId();
    Long getCreditIntentionId();
    String getCreditStatus();        // String, no enum — native query lo devuelve así
    BigDecimal getQuotaValue();
    Integer getPeriodQuantity();
    BigDecimal getTotalIntentionValue();
    BigDecimal getTotalInterestValue();
    BigDecimal getTotalCapitalValue();
    BigDecimal getTotalFinancedValue();
    Long getZoneId();
    String getZoneName();
    String getDocument();
    String getFullname();
    String getPhoneNumber();
    Long getCreditLineId();
    String getCreditLineName();
    LocalDateTime getCreatedAt();
}