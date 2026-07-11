package com.recaudo.api.infrastructure.scheduler.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un INSERT individual ejecutado durante el job.
 * Se usa para dejar traza descriptiva de cada registro persistido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertLogDto {

    /** Nombre de la tabla afectada. */
    private String tabla;

    /** Descripción legible del registro insertado (ids + valores). */
    private String descripcion;
}
