package com.recaudo.api.domain.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BasePaymentEntity {
    private Long id;
    private Long creditIntentionId;
    private Long paymentTypeId;
    private Long bankId;
    private String accountNumber;
    private Double amount;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private byte[] fileData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean status;
}