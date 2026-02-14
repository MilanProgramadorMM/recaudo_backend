package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveClosingDto {
    private Long closingId;
    private String deliveryType; // "admin", "asesor", "parcial"
    private Double amountAdmin;
    private Double amountAsesor;
}