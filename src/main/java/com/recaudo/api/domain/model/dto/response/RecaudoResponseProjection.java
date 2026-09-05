package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;

public interface RecaudoResponseProjection {

    Long getRecaudoId();
    String getClientName();
    BigDecimal getValuePaid();
    BigDecimal getInvestmentValue();
    BigDecimal getInterestValue();
    BigDecimal getLifeInsurance();
    BigDecimal getPortfolioInsurance();
    String getUserCreate();
    String getCreatedAt();
    String getZona();
    String getDescription();
    Long getNumeroCuota();
    Long getCuotasPendientes();
}
