package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreditFullResponseDto {

    private Long id;
    private Long creditIntentionId;
    private BigDecimal quotaValue;
    private Integer periodQuantity;
    private BigDecimal totalIntentionValue;
    private BigDecimal totalInterestValue;
    private BigDecimal totalCapitalValue;
    private BigDecimal totalFinancedValue;

    private Long zoneId;
    private String zoneName;
    private String document;
    private String fullname;
    private String phoneNumber;

    private Long creditLineId;
    private String creditLineName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
