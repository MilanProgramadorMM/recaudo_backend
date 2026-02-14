package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class CreditIntentionResponseDto {

    @JsonProperty("id")
    private Long id;

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

    // --- Información de crédito ---
    @JsonProperty("credit_line_id")
    private Long creditLineId;

    @JsonProperty("quota_value")
    private BigDecimal quotaValue;

    @JsonProperty("period_id")
    private Long periodId;

    @JsonProperty("period_quantity")
    private Long periodQuantity;

    @JsonProperty("tax_type_id")
    private Long taxTypeId;

    @JsonProperty("tax_value")
    private BigDecimal taxValue;

    @JsonProperty("total_intention_value")
    private BigDecimal totalIntentionValue; // valor a desembolsar

    @JsonProperty("total_interest_value")
    private BigDecimal totalInterestValue;

    @JsonProperty("total_capital_value")
    private BigDecimal totalCapitalValue;

    @JsonProperty("item_value")
    private BigDecimal itemValue;

    @JsonProperty("initial_value_payment")
    private BigDecimal initialValuePayment;

    @JsonProperty("total_financed_value")
    private BigDecimal totalFinancedValue;

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

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("date_start")
    private String dateStart;

    /*@JsonProperty("user_delete")
    private String userDelete;

    @JsonProperty("user_edit")
    private String userEdit;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @JsonProperty("edited_at")
    private LocalDateTime editedAt;

     */
}
