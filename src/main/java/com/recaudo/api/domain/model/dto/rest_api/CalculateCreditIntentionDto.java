package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class CalculateCreditIntentionDto {

    private static final String REQUIRED_MESSAGE = "Este campo es obligatorio";

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
    private Double totalIntentionValue;

    @JsonProperty("inicio_quincena")
    private Integer inicioQuincena;

    @JsonProperty("fin_quincena")
    private Integer finQuincena;

    @JsonProperty("edad")
    private Integer edad;

    @JsonProperty("tipo_calculo")
    private String tipoCalculo;

    @JsonProperty("generar_amortizacion")
    private String generarAmortizacion;

    @JsonProperty("start_date")
    private String startDate;


    /*@JsonProperty("total_interest_value")
    private Double totalInterestValue;

    @JsonProperty("total_capital_value")
    private Double totalCapitalValue;*/

    @JsonProperty("item_value")
    private Double itemValue;

    /*@JsonProperty("initial_value_payment")
    private Double initialValuePayment;

    @JsonProperty("total_financed_value")
    private Double totalFinancedValue;*/

}
