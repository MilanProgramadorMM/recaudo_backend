package com.recaudo.api.domain.model.dto.rest_api;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreditIntentionDocumentDTO {
    private Long documentationTypeId;
    private String documentSide;
    private MultipartFile file;
}