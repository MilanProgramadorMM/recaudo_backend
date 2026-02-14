package com.recaudo.api.domain.model.dto.rest_api;

import com.recaudo.api.infrastructure.helper.util.ClosingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangeClosingStatusDto {

    @NotNull
    private Long closingId;

    @NotNull
    private ClosingStatus newStatus;
}
