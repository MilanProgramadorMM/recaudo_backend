package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PeriodResponseDto {
    private String cod;
    private String name;
    private String description;
    private Integer factorConversion;
    private boolean status;
}
