package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Request
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateAgreementRequestDto {
    private Long creditId;
    private BigDecimal discountValue;
    private List<QuotaDetailDto> quotas;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuotaDetailDto {
        private Long cuotaId;
        private Integer daysLate;
        private BigDecimal pastduePeriods;
        private BigDecimal balancePending;
        private BigDecimal delayPenalty;          // interés ya calculado en frontend
    }
}