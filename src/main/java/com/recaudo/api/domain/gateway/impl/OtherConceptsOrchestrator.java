package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.gateway.OtherConceptCalculator;
import com.recaudo.api.domain.model.dto.response.ConceptComputeResult;
import com.recaudo.api.domain.model.dto.response.JobExecutionSummary;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Orquestador de conceptos adicionales (credit_other_concepts).
 *
 * Recorre todos los beans de tipo OtherConceptCalculator registrados en el
 * contexto de Spring. Por cada uno:
 *   1. Obtiene las cuotas elegibles.
 *   2. Calcula el valor del concepto por cuota.
 *   3. Persiste en credit_other_concepts + credit_other_concepts_detail.
 *
 * Para agregar un concepto nuevo solo hay que crear una clase que implemente
 * OtherConceptCalculator y anotarla con @Component. Este orquestador no
 * necesita ningún cambio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtherConceptsOrchestrator {

    private static final String KEY_TIPCON = "TIPCON";

    // Spring inyecta automáticamente TODOS los beans que implementen la interfaz
    private final List<OtherConceptCalculator>       calculators;
    private final GlotypesRepository                 glotypesRepository;
    private final CreditOtherConceptsRepository      otherConceptsRepository;
    private final CreditOtherConceptDetailRepository otherConceptDetailRepository;

    // ══════════════════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA DEL JOB
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Ejecuta todos los calculadores registrados para la fecha indicada.
     * Cada calculador define sus propias cuotas elegibles y su fórmula.
     */
    //@Transactional
    public JobExecutionSummary runAll(LocalDate today) {
        log.info("[OtherConcepts] Iniciando proceso GENERAL para {}. Calculadores activos: {}",
                today, calculators.size());

        List<JobExecutionSummary.CalculatorSummary> resultados = new ArrayList<>();

        for (OtherConceptCalculator calculator : calculators) {
            resultados.add(runSingleDetailed(calculator, today));
        }

        JobExecutionSummary resumen = JobExecutionSummary.builder()
                .fechaEjecucion(today)
                .totalCreditos(-1) // no aplica: es corrida global, no por lista de créditos
                .creditosConError(0)
                .resultadosPorCalculador(resultados)
                .erroresCredito(new ArrayList<>())
                .build();

        log.info(resumen.toLogSummary());

        return resumen;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  EJECUCIÓN POR CALCULADOR
    // ══════════════════════════════════════════════════════════════════════════



    private JobExecutionSummary.CalculatorSummary runSingleDetailed(
            OtherConceptCalculator calculator, LocalDate today) {

        String label = calculator.getLabel();
        Long glotypeId = resolveGlotypeId(calculator.getGlotypeCode());

        List<CreditAmortizationNEntity> cuotas = calculator.fetchEligibleQuotas(today);

        log.info("[OtherConcepts][{}] {} cuota(s) elegible(s) al {}", label, cuotas.size(), today);

        int registradas = 0, omitidas = 0, errores = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;
        List<JobExecutionSummary.QuotaDetail> detalle = new ArrayList<>();

        for (CreditAmortizationNEntity cuota : cuotas) {
            try {
                ConceptComputeResult resultado = calculator.compute(cuota, today);

                if (!resultado.isPresent()) {
                    log.info("[OtherConcepts][{}] creditId={} quotaNumber={} vencimiento={} -> OMITIDA ({})",
                            label, cuota.getCreditId(), cuota.getQuotaNumber(), cuota.getExpirationDate(), resultado.getReason());
                    omitidas++;
                    detalle.add(JobExecutionSummary.QuotaDetail.builder()
                            .creditId(cuota.getCreditId())
                            .quotaNumber(cuota.getQuotaNumber())
                            .expirationDate(cuota.getExpirationDate())
                            .status("OMITIDA")
                            .motivo(resultado.getReason())
                            .build());
                    continue;
                }

                persistirEnTransaccion(cuota, resultado.getValue(), glotypeId);
                valorTotal = valorTotal.add(resultado.getValue());
                registradas++;

                log.info("[OtherConcepts][{}] creditId={} quotaNumber={} vencimiento={} -> REGISTRADA valor={}",
                        label, cuota.getCreditId(), cuota.getQuotaNumber(), cuota.getExpirationDate(), resultado.getValue());

                detalle.add(JobExecutionSummary.QuotaDetail.builder()
                        .creditId(cuota.getCreditId())
                        .quotaNumber(cuota.getQuotaNumber())
                        .expirationDate(cuota.getExpirationDate())
                        .status("REGISTRADA")
                        .valor(resultado.getValue())
                        .build());

            } catch (Exception e) {
                errores++;
                log.error("[OtherConcepts][{}] creditId={} quotaNumber={} -> ERROR: {}",
                        label, cuota.getCreditId(), cuota.getQuotaNumber(), e.getMessage(), e);
                detalle.add(JobExecutionSummary.QuotaDetail.builder()
                        .creditId(cuota.getCreditId())
                        .quotaNumber(cuota.getQuotaNumber())
                        .expirationDate(cuota.getExpirationDate())
                        .status("ERROR")
                        .motivo(e.getMessage())
                        .build());
            }
        }

        log.info("[OtherConcepts][{}] Completado. evaluadas={} registradas={} omitidas={} errores={} valor_total={}",
                label, cuotas.size(), registradas, omitidas, errores, valorTotal);

        return JobExecutionSummary.CalculatorSummary.builder()
                .label(label).glotypeCode(calculator.getGlotypeCode())
                .cuotasEvaluadas(cuotas.size())
                .registradas(registradas).omitidas(omitidas).errores(errores)
                .valorTotalGenerado(valorTotal)
                .detalle(detalle)
                .build();
    }
    // ══════════════════════════════════════════════════════════════════════════
    //  PERSISTENCIA (común para todos los calculadores)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Inserta el snapshot de la cuota en credit_other_concepts
     * y el valor del concepto calculado en credit_other_concepts_detail.
     *
     * Estructura nueva:
     *  - credit_other_concepts: snapshot de la cuota (credit_id, quota_number,
     *    expiration_date, total_quota_value, liquidated, paid_full).
     *  - credit_other_concepts_detail: vincula la cuota ORIGINAL
     *    (credit_amortization.id) con el glotype y el valor calculado.
     *    No referencia al header, sino directamente a la cuota de amortización.
     */
    private void persistir(
            CreditAmortizationNEntity cuota,
            BigDecimal value,
            Long glotypeId) {

        // ── 1. Find-Or-Create del maestro ──────────────────────────────────────
        CreditOtherConceptsEntity cabecera = otherConceptsRepository
                .findByCreditIdAndQuotaNumber(cuota.getCreditId(), cuota.getQuotaNumber())
                .orElseGet(() -> {
                    // Solo se crea la primera vez (catch-up)
                    CreditOtherConceptsEntity nueva = CreditOtherConceptsEntity.builder()
                            .creditId(cuota.getCreditId())
                            .quotaNumber(cuota.getQuotaNumber())
                            .expirationDate(cuota.getExpirationDate())
                            .totalQuotaValue(cuota.getTotalQuotaValue())
                            .liquidated(cuota.getLiquidated())
                            .paidFull(cuota.getPaidFull())
                            .build();

                    log.debug("[OtherConcepts] Creando maestro nuevo: creditId={} quotaNumber={}",
                            cuota.getCreditId(), cuota.getQuotaNumber());

                    return otherConceptsRepository.save(nueva);
                });

        // ── 2. Siempre inserta un detalle nuevo (acumula por día) ──────────────
        CreditOtherConceptDetailEntity detalle = CreditOtherConceptDetailEntity.builder()
                .creditOtherConceptId(cabecera.getId())
                .conceptId(glotypeId)
                .value(value)
                .createdAt(LocalDateTime.now())
                .userCreate("SYSTEM")
                .build();

        otherConceptDetailRepository.save(detalle);

        log.debug("[OtherConcepts] Detalle persistido: maestroId={} creditId={} quotaNumber={} valor={}",
                cabecera.getId(), cuota.getCreditId(), cuota.getQuotaNumber(), value);
    }

    /* @Transactional
    public void runForCredit(LocalDate today, Long creditId) {
        log.info("[OtherConcepts] Iniciando proceso para creditId={} fecha={}", creditId, today);

        for (OtherConceptCalculator calculator : calculators) {
            String label = calculator.getLabel();
            Long glotypeId = resolveGlotypeId(calculator.getGlotypeCode());

            List<CreditAmortizationNEntity> cuotas =
                    calculator.fetchEligibleQuotasByCreditId(today, creditId);

            if (cuotas.isEmpty()) {
                log.info("[OtherConcepts][{}] Sin cuotas elegibles para creditId={}", label, creditId);
                continue;
            }

            for (CreditAmortizationNEntity cuota : cuotas) {
                try {
                    Optional<BigDecimal> resultado = calculator.compute(cuota, today);
                    if (resultado.isEmpty()) continue;
                    persistir(cuota, resultado.get(), glotypeId);
                } catch (Exception e) {
                    log.error("[OtherConcepts][{}] Error en cuota={}: {}", label, cuota.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("[OtherConcepts] Proceso completado para creditId={}", creditId);
    }

    public void runForCredits(LocalDate today, List<Long> creditIds) {
        log.info("[OtherConcepts] Iniciando proceso para {} créditos — fecha={}", creditIds.size(), today);

        for (Long creditId : creditIds) {
            try {
                runForCredit(today, creditId);
            } catch (Exception e) {
                log.error("[OtherConcepts] Error procesando creditId={}: {}", creditId, e.getMessage(), e);
                // continúa con el siguiente
            }
        }

        log.info("[OtherConcepts] Proceso completado para {} créditos", creditIds.size());
    }

     */

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private Long resolveGlotypeId(String gloCode) {
        return glotypesRepository.findByKeyAndCode(KEY_TIPCON, gloCode)
                .map(GlotypesEntity::getId)
                .orElseThrow(() -> new RuntimeException("Glotype no encontrado: " + gloCode));
    }

    @Transactional
    private void persistirEnTransaccion(CreditAmortizationNEntity cuota, BigDecimal value, Long glotypeId) {
        persistir(cuota, value, glotypeId);
    }

    public JobExecutionSummary.CalculatorSummary runForCreditDetailed(
            OtherConceptCalculator calculator, LocalDate today, Long creditId) {

        String label = calculator.getLabel();
        Long glotypeId = resolveGlotypeId(calculator.getGlotypeCode());

        List<CreditAmortizationNEntity> cuotas =
                calculator.fetchEligibleQuotasByCreditId(today, creditId);

        int registradas = 0, omitidas = 0, errores = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;
        List<JobExecutionSummary.QuotaDetail> detalle = new ArrayList<>();

        log.info("[OtherConcepts][{}] creditId={} -> {} cuota(s) elegible(s) al {}",
                label, creditId, cuotas.size(), today);

        for (CreditAmortizationNEntity cuota : cuotas) {
            try {
                ConceptComputeResult resultado = calculator.compute(cuota, today);

                if (!resultado.isPresent()) {
                    log.info("[OtherConcepts][{}] creditId={} quotaNumber={} vencimiento={} -> OMITIDA ({})",
                            label, creditId, cuota.getQuotaNumber(), cuota.getExpirationDate(), resultado.getReason());
                    omitidas++;
                    detalle.add(JobExecutionSummary.QuotaDetail.builder()
                            .creditId(creditId)
                            .quotaNumber(cuota.getQuotaNumber())
                            .expirationDate(cuota.getExpirationDate())
                            .status("OMITIDA")
                            .motivo(resultado.getReason())
                            .build());
                    continue;
                }

                persistir(cuota, resultado.getValue(), glotypeId);
                valorTotal = valorTotal.add(resultado.getValue());
                registradas++;

                log.info("[OtherConcepts][{}] creditId={} quotaNumber={} vencimiento={} -> REGISTRADA valor={}",
                        label, creditId, cuota.getQuotaNumber(), cuota.getExpirationDate(), resultado.getValue());

                detalle.add(JobExecutionSummary.QuotaDetail.builder()
                        .creditId(creditId)
                        .quotaNumber(cuota.getQuotaNumber())
                        .expirationDate(cuota.getExpirationDate())
                        .status("REGISTRADA")
                        .valor(resultado.getValue())
                        .build());

            } catch (Exception e) {
                errores++;
                log.error("[OtherConcepts][{}] creditId={} quotaNumber={} -> ERROR: {}",
                        label, creditId, cuota.getQuotaNumber(), e.getMessage(), e);
                detalle.add(JobExecutionSummary.QuotaDetail.builder()
                        .creditId(creditId)
                        .quotaNumber(cuota.getQuotaNumber())
                        .expirationDate(cuota.getExpirationDate())
                        .status("ERROR")
                        .motivo(e.getMessage())
                        .build());
            }
        }

        log.info("[OtherConcepts][{}] creditId={} completado. registradas={} omitidas={} errores={} valor_total={}",
                label, creditId, registradas, omitidas, errores, valorTotal);

        return JobExecutionSummary.CalculatorSummary.builder()
                .label(label).glotypeCode(calculator.getGlotypeCode())
                .cuotasEvaluadas(cuotas.size())
                .registradas(registradas).omitidas(omitidas).errores(errores)
                .valorTotalGenerado(valorTotal)
                .detalle(detalle)
                .build();
    }
    @Transactional(rollbackFor = Exception.class)
    public JobExecutionSummary runForCredits(LocalDate today, List<Long> creditIds) {
        log.info("[OtherConcepts] Iniciando proceso TODO-O-NADA para {} credito(s) — fecha={}",
                creditIds.size(), today);

        Map<String, JobExecutionSummary.CalculatorSummary> acumulado = new LinkedHashMap<>();

        for (Long creditId : creditIds) {
            for (OtherConceptCalculator calculator : calculators) {
                JobExecutionSummary.CalculatorSummary parcial =
                        runForCreditDetailed(calculator, today, creditId);

                acumulado.merge(calculator.getGlotypeCode(), parcial, (acc, nuevo) -> {
                    List<JobExecutionSummary.QuotaDetail> detalleUnido = new ArrayList<>(acc.getDetalle());
                    detalleUnido.addAll(nuevo.getDetalle());

                    return JobExecutionSummary.CalculatorSummary.builder()
                            .label(acc.getLabel())
                            .glotypeCode(acc.getGlotypeCode())
                            .cuotasEvaluadas(acc.getCuotasEvaluadas() + nuevo.getCuotasEvaluadas())
                            .registradas(acc.getRegistradas() + nuevo.getRegistradas())
                            .omitidas(acc.getOmitidas() + nuevo.getOmitidas())
                            .errores(acc.getErrores() + nuevo.getErrores())
                            .valorTotalGenerado(acc.getValorTotalGenerado().add(nuevo.getValorTotalGenerado()))
                            .detalle(detalleUnido)
                            .build();
                });
            }
        }

        JobExecutionSummary resumen = JobExecutionSummary.builder()
                .fechaEjecucion(today)
                .totalCreditos(creditIds.size())
                .creditosConError(0)
                .resultadosPorCalculador(new ArrayList<>(acumulado.values()))
                .erroresCredito(new ArrayList<>())
                .build();

        log.info(resumen.toLogSummary());

        return resumen;
    }
}