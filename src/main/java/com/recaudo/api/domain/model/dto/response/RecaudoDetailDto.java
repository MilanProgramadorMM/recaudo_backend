package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecaudoDetailDto {
    private Long recaudoId;
    private Integer quotaNumber;
    private String conceptName;
    private BigDecimal valuePaid;
    private BigDecimal investmentValue;
    private BigDecimal interestValue;
    private BigDecimal lifeInsurance;
    private BigDecimal portfolioInsurance;
    private String userCreate;
    private String createdAt;
    private BigDecimal delayPenalty;

}