package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditIntentionPaymentResponseDto {
    private Long id;
    private Long creditIntentionId;
    private Long paymentTypeId;
    private String paymentTypeName;
    private Long bankId;
    private String bankName;
    private String accountNumber;
    private Double amount;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String createdAt;
}