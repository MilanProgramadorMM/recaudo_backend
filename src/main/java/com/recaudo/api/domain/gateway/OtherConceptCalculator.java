package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.entity.CreditAmortizationNEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato que debe implementar cada calculador de "otros conceptos".
 *
 * Para registrar un concepto nuevo basta con crear una clase que implemente
 * esta interfaz y anotarla con @Component. El orquestador la detecta
 * automáticamente a través de la inyección de List<OtherConceptCalculator>.
 *
 * Responsabilidades de cada implementación:
 *  1. Decir qué cuotas le aplican (fetchEligibleQuotas).
 *  2. Calcular el valor del concepto para una cuota puntual (compute).
 *  3. Exponer los códigos de glotype y concepto contable que usará el
 *     orquestador al persistir.
 */
public interface OtherConceptCalculator {

    /**
     * Código del glotype que identifica este concepto en la tabla glotypes.
     * Ej: "IMT" para interés moratorio.
     */
    String getGlotypeCode();

    /**
     * Etiqueta legible para logs.
     * Ej: "Interés Moratorio".
     */
    String getLabel();

    /**
     * Retorna la lista de cuotas que aplican para este concepto en la
     * fecha indicada. Cada implementación define sus propias condiciones
     * de filtrado (vencidas, activas, con seguro, etc.).
     */
    List<CreditAmortizationNEntity> fetchEligibleQuotas(LocalDate today);

    /**
     * Calcula el valor del concepto para una cuota específica.
     *
     * @return Optional.empty() si el valor es cero o no aplica
     *         (el orquestador omite el registro en ese caso).
     */
    Optional<BigDecimal> compute(CreditAmortizationNEntity cuota, LocalDate today);
}