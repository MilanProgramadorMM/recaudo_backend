package com.recaudo.api.domain.model.dto.rest_api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReverseRecaudoRequestDto {
    @NotNull(message = "El ID del crédito es obligatorio")
    private Long creditId;
    
    @NotNull(message = "Debe seleccionar al menos un recaudo a reversar")
    @Size(min = 1, message = "Debe seleccionar al menos un recaudo")
    private List<Long> recaudoIds;
}

