package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditIntentionDocumentResponseDto {
    private Long id;
    private Long documentationTypeId;
    private String documentSide;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String fileDataBase64;
}
