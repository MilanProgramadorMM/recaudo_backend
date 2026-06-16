package com.recaudo.api.domain.model.dto.response;

public interface PersonInterfaceResponseDto {

    Long getId();
    Long getDocumentType();
    String getDocument();
    String getFirstName();
    String getMiddleName();
    String getLastName();
    String getMaternalLastname();
    String getFullName();
    Long getGender();
    String getOccupation();
    String getDescription();
    Boolean getStatus();
    String getUsername();
    Long getOrden();
    String getZid();
    String getZona();
    Long getCountryId();
    String getCountry();
    Long getDepartentId();
    String getDepartent();
    Long getCityId();
    String getCity();
    Long getNeighborhoodId();
    String getNeighborhood();
    String getAdress();
    String getCorreo();
    String getCelular();
    String getWhatsApp();
    String getDescriptionD();
    Integer getDiasMora();
    String getRatingCredit();
}
