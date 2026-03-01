package com.recaudo.api.domain.model.dto.rest_api;

import com.recaudo.api.infrastructure.helper.util.ApprovalStatus;
import lombok.*;

import java.math.BigDecimal;

// Response pública con info de la intención (sin datos sensibles)
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PublicCreditIntentionResponse {
    private Long id;
    private String fullname;
    private String nameLine;
    private BigDecimal quotaValue;
    private Integer periodQuantity;
    private String namePeriod;
    private BigDecimal totalCapitalValue;
    private ApprovalStatus approvalStatus;
    private boolean tokenExpired;
    // Getters y setters
}