package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;

public interface IntentionCreditResponseAllDto {

    Long getId();

    Long getZoneId();
    Long getPersonId();
    String getZoneName();
    String getZoneDescription();

    String getDocumentType();
    String getDocument();

    String getFirstname();
    String getMiddlename();
    String getLastname();
    String getMaternalLastname();
    String getFullname();

    String getGender();
    String getGenero();
    String getOccupation();
    String getDescription();

    String getEmail();
    String getPhoneNumber();
    String getWhatsappNumber();

    Long getCreditLineId();
    String getCreditLineName();

    BigDecimal getQuotaValue();

    Long getPeriodId();
    String getPeriodName();
    String getPeriodCode();
    Integer getPeriodQuantity();

    Long getTaxTypeId();
    String getTaxTypeName();
    BigDecimal getTaxValue();

    BigDecimal getTotalIntentionValue();
    BigDecimal getTotalInterestValue();
    BigDecimal getTotalCapitalValue();
    BigDecimal getItemValue();
    BigDecimal getInitialValuePayment();
    BigDecimal getTotalFinancedValue();
    BigDecimal getStationery();

    String getHomeAddress();

    Long getCountryId();
    String getCountryName();

    Long getDepartmentId();
    String getDepartmentName();

    Long getMunicipalityId();
    String getMunicipalityName();

    Long getNeighborhoodId();
    String getNeighborhoodName();

    String getCreatedAt();
    String getEditedAt();
    Integer getClientExists();
    Integer getInitialQuincena();
    Integer getEndQuincena();
    Boolean getReferido();
    Boolean getCallSuccess();
    String getEstadoActual();

    String getApprovalLink();
    String getApprovalToken();
    String getApprovalStatus();
    String getApprovedAt();
    String getApprovalIp();
    String getTokenExpiresAt();
    String getFechaInicio();

    String getRatingCredit();
    Integer getRatingStart();
    Integer getRatingEnd();

}
