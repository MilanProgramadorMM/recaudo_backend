package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClientDataCreditIntentionUpdateDto {

    // --- Datos personales ---
    @JsonProperty("zone_id")
    private Long zoneId;

    @JsonProperty("document_type")
    private Long documentType;

    @JsonProperty("document")
    private String document;

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("middlename")
    private String middlename;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("maternal_lastname")
    private String maternalLastname;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("gender")
    private Long gender;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("description")
    private String description;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("whatsapp_number")
    private String whatsappNumber;

    // --- Dirección / ubicación ---
    @JsonProperty("home_address")
    private String homeAddress;

    @JsonProperty("country_id")
    private Long countryId;

    @JsonProperty("department_id")
    private Long departmentId;

    @JsonProperty("municipality_id")
    private Long municipalityId;

    @JsonProperty("neighborhood_id")
    private Long neighborhoodId;

    @JsonProperty("referido")
    private Boolean referido;

    @JsonProperty("call_success")
    private Boolean callSuccess;

    @JsonProperty("initial_quincena")
    private Integer initialQuincena;

    @JsonProperty("end_quincena")
    private Integer endQuincena;


}
