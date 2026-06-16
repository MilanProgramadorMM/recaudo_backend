package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record DashboardHistorialDto(
    LocalDate fecha,
    BigDecimal valor
) {}