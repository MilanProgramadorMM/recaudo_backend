package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClosingSpendDto {

    private Long closingId;
    private Long spendTypeId;
    private Double amount;
    private String description;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private MultipartFile file;
}
