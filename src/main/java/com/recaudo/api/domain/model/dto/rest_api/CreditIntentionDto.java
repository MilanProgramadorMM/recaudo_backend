package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.Double;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class CreditIntentionDto {

    private static final String REQUIRED_MESSAGE = "Este campo es obligatorio";

    // --- Datos personales ---
    @NotNull(message = REQUIRED_MESSAGE)
    @JsonProperty("zone_id")
    private Long zoneId;

    @NotNull(message = REQUIRED_MESSAGE)
    @JsonProperty("document_type")
    private Long documentType;

    @NotBlank(message = REQUIRED_MESSAGE)
    @JsonProperty("document")
    private String document;

    @NotBlank(message = REQUIRED_MESSAGE)
    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("middlename")
    private String middlename;

    @NotBlank(message = REQUIRED_MESSAGE)
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

    // --- Información de crédito ---
    @JsonProperty("credit_line_id")
    private Long creditLineId;

    @JsonProperty("quota_value")
    private Double quotaValue;

    @JsonProperty("period_id")
    private Long periodId;

    @JsonProperty("period_code")
    private String periodCode;

    @JsonProperty("period_quantity")
    private Integer periodQuantity;

    @JsonProperty("tax_type_id")
    private Long taxTypeId;

    @JsonProperty("tax_value")
    private Double taxValue;

    @JsonProperty("total_intention_value")
    private Double totalIntentionValue; // valor a desembolsar

    @JsonProperty("inicio_quincena")
    private Integer inicioQuincena;

    @JsonProperty("fin_quincena")
    private Integer finQuincena;

    @JsonProperty("tipo_calculo")
    private String tipoCalculo;

    @JsonProperty("start_date")
    private String startDate;


    /*@JsonProperty("total_interest_value")
    private Double totalInterestValue;

    @JsonProperty("total_capital_value")
    private Double totalCapitalValue;*/

    @JsonProperty("item_value")
    private Double itemValue;

    @JsonProperty("initial_value_payment")
    private Double initialValuePayment;

    @JsonProperty("total_financed_value")
    private Double totalFinancedValue;

    // --- Ubicación ---
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

    // --- Auditoría ---
    @JsonProperty("user_create")
    private String userCreate;

    @Builder.Default
    @JsonProperty("created_at")
    private String createdAt = LocalDateTime.now().toString();

}
