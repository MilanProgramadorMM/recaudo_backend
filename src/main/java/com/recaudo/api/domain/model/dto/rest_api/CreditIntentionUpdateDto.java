package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreditIntentionUpdateDto {

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

    @JsonProperty("item_value")
    private Double itemValue;

    @JsonProperty("initial_value_payment")
    private Double initialValuePayment;

    @JsonProperty("total_financed_value")
    private Double totalFinancedValue;

    @JsonProperty("total_intention_value")
    private Double totalIntentionValue;

    @JsonProperty("inicio_quincena")
    private Integer inicioQuincena;

    @JsonProperty("fin_quincena")
    private Integer finQuincena;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("tipo_calculo")
    private String tipoCalculo;
}
