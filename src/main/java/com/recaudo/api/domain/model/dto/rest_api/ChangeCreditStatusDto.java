package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.recaudo.api.domain.model.constant.CreditStatusCode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ChangeCreditStatusDto {

    @NotNull
    @JsonProperty("credit_id")
    private Long creditId;

    @NotNull
    @JsonProperty("new_status")
    private CreditStatusCode newStatus;
}
