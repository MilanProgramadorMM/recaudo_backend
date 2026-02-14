package com.recaudo.api.infrastructure.helper.util;

import lombok.Data;

@Data
public class DocumentMetadata {
    private Long documentationTypeId;
    private String documentSide;
    private Integer fileIndex;
}