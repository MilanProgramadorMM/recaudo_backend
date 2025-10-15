package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BarrioResponseDto {

    Long id;
    String nombre;
    String description;
    private Long idMunicipio;
    private String nombreMunicipio;

    private Long idDepartamento;
    private String nombreDepartamento;

    private Long idPais;
    private String nombrePais;
}
