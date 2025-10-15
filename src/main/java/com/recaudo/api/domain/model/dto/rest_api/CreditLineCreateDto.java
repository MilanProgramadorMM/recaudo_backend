package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLineCreateDto {

    @NotBlank(message = "El nombre es obligatorio")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "El valor mínimo de cuota es obligatorio")
    @JsonProperty("min_quota")
    private BigDecimal minQuota;

    @NotNull(message = "El valor máximo de cuota es obligatorio")
    @JsonProperty("max_quota")
    private BigDecimal maxQuota;

    @NotNull(message = "El período mínimo es obligatorio")
    @Min(value = 1, message = "El período mínimo debe ser al menos 1")
    @JsonProperty("min_period")
    private Integer minPeriod;

    @NotNull(message = "El período máximo es obligatorio")
    @Min(value = 1, message = "El período máximo debe ser al menos 1")
    @JsonProperty("max_period")
    private Integer maxPeriod;

    @NotNull(message = "El tipo de impuesto es obligatorio")
    @JsonProperty("tax_type_id")
    private Long taxType;

    @NotNull(message = "El tipo de amortización es obligatorio")
    @JsonProperty("amortization_type_id")
    private Long amortizationType;

    @JsonProperty("procedure_name")
    private String procedureName;

    @JsonProperty("life_insurance")
    private boolean lifeInsurance;

    @JsonProperty("portfolio_insurance")
    private boolean portfolioInsurance;

    @JsonProperty("require_documentation")
    private boolean requireDocumentation;

    //@JsonProperty("documentation_type_ids")
    //private List<Long> documentationTypeIds;

}
