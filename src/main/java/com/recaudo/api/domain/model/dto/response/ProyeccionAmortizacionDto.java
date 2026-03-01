package com.recaudo.api.domain.model.dto.response;

import java.time.LocalDate;

public interface ProyeccionAmortizacionDto {

    Integer getDcreNumcuota();
    Double getDcreVlrcuota();
    Double getDcreTasa();
    Double getDcreCapital();

    LocalDate getDcreFvence();
    Double getDcreSaldocapital();
    Double getDcreVlrabonoinversion();
    Double getDcreVlrabonointeres();
    Double getDcreVlrabonosegurocartera();
    Double getDcreVlrabonosegurovida();
    Double getDcreCapitalBase();
}
