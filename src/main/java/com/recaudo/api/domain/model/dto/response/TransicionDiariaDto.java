package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Transiciones de la cartera entre un snapshot y el snapshot inmediatamente anterior
 * de la zona. Cada valor es un conteo de créditos que sufrieron ese cambio ese día.
 * Pensado para gráficas de barras de comportamiento diario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransicionDiariaDto {

    private LocalDate fecha;

    private Long ingresaronMora;
    private Long salieronMora;
    private Long cambiaronEstado;
    private Long cancelados;
    private Long cambiosCalificacion;
    private Long nuevosCreditos;
    private Long creditosQueSalieron;
}
