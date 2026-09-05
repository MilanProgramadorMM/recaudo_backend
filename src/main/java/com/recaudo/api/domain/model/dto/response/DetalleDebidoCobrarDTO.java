package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalleDebidoCobrarDTO(
        Long creditId,
        Long cuotaId,
        Integer quotaNumber,
        LocalDate expirationDate,
        String clientName,
        Integer clientOrden,
        BigDecimal valorCuotaNominal,
        BigDecimal valorCuotaPendiente,
        String zonaCode,
        String zona,
        String nombreDia
) {}