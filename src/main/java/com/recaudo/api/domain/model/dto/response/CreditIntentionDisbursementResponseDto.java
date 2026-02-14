package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditIntentionDisbursementResponseDto {
    
    private Long id;
    private Long creditIntentionId;
    private Long paymentTypeId;
    private String paymentTypeName;
    private Long bankId;
    private String bankName;
    private String accountNumber;
    private Double amount;
    
    // Información del archivo
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String fileData;  // Base64 si se necesita
    
    private String createdAt;
}