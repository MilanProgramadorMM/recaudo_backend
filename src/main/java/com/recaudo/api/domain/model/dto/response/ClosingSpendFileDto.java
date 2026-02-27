package com.recaudo.api.domain.model.dto.response;

import lombok.Data;

@Data
public class ClosingSpendFileDto {

    private String fileName;
    private String contentType;
    private byte[] fileData;

    public ClosingSpendFileDto(String fileName, String contentType, byte[] fileData) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileData = fileData;
    }


}