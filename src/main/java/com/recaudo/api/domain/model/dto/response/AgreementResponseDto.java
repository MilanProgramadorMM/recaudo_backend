package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Response maestro
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AgreementResponseDto {
    private Long id;
    private Long creditId;
    private BigDecimal projectedValue;
    private BigDecimal discountValue;
    private BigDecimal agreedValue;
    private String paymentDate;
    private Boolean status;
    private String userCreate;
    private String createdAt;
    private List<AgreementDetailResponseDto> detalles;
}