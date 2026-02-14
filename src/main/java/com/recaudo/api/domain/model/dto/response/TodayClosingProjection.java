package com.recaudo.api.domain.model.dto.response;

import java.time.LocalDate;

public interface TodayClosingProjection {

    Long getClosingId();
    String getClosingStatus();
    String getClosingDate();
    Long getPersonId();
    Long getZonaId();
}

