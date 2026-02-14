package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodayClosingResponseDto {

    private boolean hasClosingToday;
    private ClosingResponseDto closing;

}
