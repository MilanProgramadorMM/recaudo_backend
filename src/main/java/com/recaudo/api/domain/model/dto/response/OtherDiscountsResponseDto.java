package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OtherDiscountsResponseDto {

    private Long id;
    private String name;
    private String description;
    private String procedure;
    private String status;
}
