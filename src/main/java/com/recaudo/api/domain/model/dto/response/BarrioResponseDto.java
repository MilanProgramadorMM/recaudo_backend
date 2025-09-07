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
    String nombreMunicipio;
    Long idMunicipio;
    String nombreDepartamento;
    String nombrePais;
}
