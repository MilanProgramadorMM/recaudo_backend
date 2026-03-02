package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationResponseDto {

    private Integer dcreNumcuota;
    private Double dcreVlrcuota;
    private Double dcreTasa;
    private Double dcreCapital;
    private LocalDate dcreFvence;
    private Double dcreSaldocapital;
    private Double dcreVlrabonoinversion;
    private Double dcreVlrabonointeres;
    private Double dcreVlrabonosegurocartera;
    private Double dcreVlrabonosegurovida;
    private Double dcreVlrBase;
    private Double dcreVlrPapeleia;
    private Double dcreVlrBasePapeleria;


}
