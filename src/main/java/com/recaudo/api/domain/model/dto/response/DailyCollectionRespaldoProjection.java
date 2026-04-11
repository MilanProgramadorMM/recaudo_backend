package com.recaudo.api.domain.model.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DailyCollectionRespaldoProjection {

    Long getCreditId();
    Integer getGrupoId();
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime getFechaInicio();
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime getFechaFin();
    BigDecimal getTotalPagado();
    String getNombreDia();
}