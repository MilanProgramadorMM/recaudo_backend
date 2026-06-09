package com.recaudo.api.domain.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public record DashboardNoPagoSummaryDto(
    BigDecimal totalValue,
    Long totalCantidad
) {}