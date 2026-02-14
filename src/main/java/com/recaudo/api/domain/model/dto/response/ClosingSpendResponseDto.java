package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClosingSpendResponseDto {

    private Long id;
    private Long closingId;
    private Long spendTypeId;
    private Double amount;
    private String typeSpend;
    private String description;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String fileData;
    private String userCreate;
    private String userEdit;
    private String createdAt;
    private String editedAt;
    private Boolean status;
    private Long zona;
}
