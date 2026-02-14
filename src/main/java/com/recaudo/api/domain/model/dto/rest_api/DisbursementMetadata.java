package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementMetadata {

    private Long id;
    private Long paymentTypeId;
    private Long bankId;
    private String accountNumber;
    private Double amount;
    private Boolean hasFile;
}