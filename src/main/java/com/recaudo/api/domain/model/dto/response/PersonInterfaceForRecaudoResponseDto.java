package com.recaudo.api.domain.model.dto.response;

public interface PersonInterfaceForRecaudoResponseDto {

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
    Long getOrden();
    Long getZonaId();
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
    String getTelefono();
    String getDescriptionD();
    Long getCreditId();
    Double getCreditAmount();
    Double getCreditBalance();
    String getCreditStatus();


}
