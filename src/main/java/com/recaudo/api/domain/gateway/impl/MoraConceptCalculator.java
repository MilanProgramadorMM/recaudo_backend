package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.gateway.OtherConceptCalculator;
import com.recaudo.api.domain.model.entity.CreditAmortizationDetailEntity;
import com.recaudo.api.domain.model.entity.CreditAmortizationNEntity;
import com.recaudo.api.domain.model.entity.CreditEntity;
import com.recaudo.api.domain.model.entity.GlotypesEntity;
import com.recaudo.api.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Calculador de interés moratorio (IMT).
 *
 * Maneja dos escenarios:
 *
 *  1. CATCH-UP (primera ejecución sobre la cuota):
 *     No existe ningún registro previo en credit_other_concepts_detail
 *     para esta cuota + glotype IMT. Se calculan TODOS los días vencidos
 *     desde expiration_date hasta hoy en un solo registro.
 *
 *  2. DIARIO (ejecuciones posteriores):
 *     Ya existe al menos un registro previo. Se calcula únicamente
 *     el valor de 1 día de mora.
 *
 * Fórmula (común a ambos casos, varía solo diasMora):
 *   periodosVencidos = diasMora / 30
 *   valorMora        = (saldoPendiente × tasaNominal) × periodosVencidos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MoraConceptCalculator implements OtherConceptCalculator {

    private static final String KEY_TIPCON      = "TIPCON";
    private static final String GLO_CAPITAL     = "VIV";
    private static final String GLO_INTERES     = "VIT";
    private static final String GLO_SEG_VIDA    = "SV";
    private static final String GLO_SEG_CARTERA = "SC";
    private static final String GLO_MORA        = "IMT";

    private final CreditAmortizationNRepository      amortizationRepository;
    private final CreditAmortizationDetailRepository amortizationDetailRepository;
    private final RecaudoDetailRepository            recaudoDetailRepository;
    private final CreditOtherConceptDetailRepository otherConceptDetailRepository;
    private final CreditRepository                   creditRepository;
    private final GlotypesRepository                 glotypesRepository;
    private final HolidaysRepository holidaysRepository;

    // ── Contrato ──────────────────────────────────────────────────────────────

    @Override
    public String getGlotypeCode() { return GLO_MORA; }

    @Override
    public String getLabel() { return "Interés Moratorio"; }

    @Override
    public List<CreditAmortizationNEntity> fetchEligibleQuotas(LocalDate today) {
        return amortizationRepository.findOverdueQuotas(today);
    }

    /**
     * Determina cuántos días usar y delega al método de cálculo puro.
     *
     *  - Sin historial previo → diasMora = días reales desde vencimiento (catch-up)
     *  - Con historial previo → diasMora = 1 (registro diario)
     */
    @Override
    public Optional<BigDecimal> compute(CreditAmortizationNEntity cuota, LocalDate today) {
        try {
            if (esNoHabil(today)) {
                return Optional.empty();
            }
            long diasTotalesVencidos = ChronoUnit.DAYS.between(cuota.getExpirationDate(), today);

            // diasTotalesVencidos == 0 → vence hoy   → registrar 1 día
            // diasTotalesVencidos >  0 → vencida      → catch-up o diario
            // diasTotalesVencidos <  0 → cuota futura → no aplica
            if (diasTotalesVencidos < 0) return Optional.empty();

            BigDecimal saldoPendiente = resolverSaldoPendiente(cuota);
            if (saldoPendiente.compareTo(BigDecimal.ZERO) <= 0) return Optional.empty();

            CreditEntity credit = creditRepository.findById(cuota.getCreditId())
                    .orElseThrow(() -> new RuntimeException(
                            "Crédito no encontrado: " + cuota.getCreditId()));

            BigDecimal tasaNominal = credit.getTaxValue()
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            long diasAUsar;
            if (diasTotalesVencidos == 0) {
                // Vence hoy: siempre 1 día, independiente del historial
                diasAUsar = 1L;
            } else if(tieneMoraPrevia(cuota)){
                diasAUsar = 1L;
            }else{
                diasAUsar = contarDiasHabiles(cuota.getExpirationDate(), today);
                log.debug("[{}] cuotaId={} | catch-up: {} días hábiles de {} días calendario",
                        getLabel(), cuota.getId(), diasAUsar, diasTotalesVencidos);
            }

            if (diasAUsar <= 0) {
                log.debug("[{}] cuotaId={} | sin días hábiles que calcular, se omite.",
                        getLabel(), cuota.getId());
                return Optional.empty();
            }

            BigDecimal mora = computeMora(saldoPendiente, tasaNominal, diasAUsar);

            log.debug("[{}] cuotaId={} | saldo={} | diasUsados={} (totalVencidos={}) | mora={}",
                    getLabel(), cuota.getId(), saldoPendiente,
                    diasAUsar, diasTotalesVencidos, mora);

            return mora.compareTo(BigDecimal.ZERO) > 0
                    ? Optional.of(mora)
                    : Optional.empty();

        } catch (Exception e) {
            log.error("[{}] Error calculando cuota={}: {}",
                    getLabel(), cuota.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CÁLCULO PURO DE MORA
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Calcula el interés moratorio dado un saldo, una tasa y una cantidad de días.
     *
     * Parametrizar diasMora permite reutilizar la misma fórmula tanto para
     * el catch-up (N días) como para el registro diario (1 día).
     *
     *   periodosVencidos = diasMora / 30
     *   valorMora        = (saldoPendiente × tasaNominal) × periodosVencidos
     *
     * @param saldoPendiente saldo que aún debe la cuota
     * @param tasaNominal    tasa del crédito ya dividida entre 100
     * @param diasMora       días a considerar: 1 para cálculo diario,
     *                       N para catch-up de varios días
     * @return valor de mora redondeado a 2 decimales
     */
    public BigDecimal computeMora(
            BigDecimal saldoPendiente,
            BigDecimal tasaNominal,
            long diasMora) {

        if (diasMora <= 0
                || saldoPendiente.compareTo(BigDecimal.ZERO) <= 0
                || tasaNominal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal periodosVencidos = BigDecimal.valueOf(diasMora)
                .divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP);

        return saldoPendiente
                .multiply(tasaNominal)
                .multiply(periodosVencidos)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Verifica si ya existe al menos un registro de mora para esta cuota.
     * La consulta va via JOIN credit_other_concepts (creditId + quotaNumber)
     * ya que el detalle no referencia directamente a credit_amortization.
     */
    private boolean tieneMoraPrevia(CreditAmortizationNEntity cuota) {
        Long idMora = resolveGlotypeId(GLO_MORA);
        return otherConceptDetailRepository.existsByCreditAndQuotaAndConcept(
                cuota.getCreditId(), cuota.getQuotaNumber(), idMora);
    }

    /**
     * Saldo pendiente real = valor bruto (amortización detalle)
     *                        − lo ya abonado (recaudo detalle, valor absoluto).
     *
     * Se considera pago parcial: si abonaron 25.000 de 50.000,
     * el saldo sobre el que se calcula mora es 25.000.
     */
    private BigDecimal resolverSaldoPendiente(CreditAmortizationNEntity cuota) {
        Long idCapital = resolveGlotypeId(GLO_CAPITAL);
        Long idInteres = resolveGlotypeId(GLO_INTERES);
        Long idSegVida = resolveGlotypeId(GLO_SEG_VIDA);
        Long idSegCart = resolveGlotypeId(GLO_SEG_CARTERA);

        List<CreditAmortizationDetailEntity> amorDetails =
                amortizationDetailRepository.findByAmortizationId(cuota.getId());

        BigDecimal totalEsperado = sumAmortDetail(amorDetails, idCapital)
                .add(sumAmortDetail(amorDetails, idInteres))
                .add(sumAmortDetail(amorDetails, idSegVida))
                .add(sumAmortDetail(amorDetails, idSegCart));

        BigDecimal totalPagado =
                recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idCapital).abs()
                        .add(recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idInteres).abs())
                        .add(recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idSegVida).abs())
                        .add(recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idSegCart).abs());

        BigDecimal pendiente = totalEsperado.subtract(totalPagado);
        return pendiente.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pendiente;
    }

    private Long resolveGlotypeId(String gloCode) {
        return glotypesRepository.findByKeyAndCode(KEY_TIPCON, gloCode)
                .map(GlotypesEntity::getId)
                .orElseThrow(() -> new RuntimeException("Glotype no encontrado: " + gloCode));
    }

    private BigDecimal sumAmortDetail(
            List<CreditAmortizationDetailEntity> details, Long conceptId) {
        return details.stream()
                .filter(d -> Objects.equals(d.getConceptId().longValue(), conceptId))
                .map(CreditAmortizationDetailEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean esNoHabil(LocalDate fecha) {
        if (fecha.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            log.debug("[{}] Fecha {} es domingo, se omite cálculo de mora.", getLabel(), fecha);
            return true;
        }
        if (holidaysRepository.existsByHoliDateAndActive(fecha)) {
            log.debug("[{}] Fecha {} es festivo, se omite cálculo de mora.", getLabel(), fecha);
            return true;
        }
        return false;
    }

    /**
     * Cuenta los días hábiles entre dos fechas (exclusivo en startDate, inclusivo en endDate).
     * Excluye domingos y festivos activos.
     */
    private long contarDiasHabiles(LocalDate desde, LocalDate hasta) {
        List<java.sql.Date> festivosRaw = holidaysRepository.findActiveBetween(desde, hasta);

        // Conversión java.sql.Date → LocalDate
        java.util.Set<LocalDate> festivosSet = festivosRaw.stream()
                .map(java.sql.Date::toLocalDate)
                .collect(java.util.stream.Collectors.toSet());

        long diasHabiles = 0;
        long diasOmitidos = 0;
        LocalDate fecha = desde.plusDays(1);

        while (!fecha.isAfter(hasta)) {
            boolean esDomingo = fecha.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
            boolean esFestivo = festivosSet.contains(fecha);

            if (esDomingo || esFestivo) {
                log.debug("[MoraCatchUp] Omitiendo {} — {}",
                        fecha, esDomingo ? "DOMINGO" : "FESTIVO");
                diasOmitidos++;
            } else {
                diasHabiles++;
            }
            fecha = fecha.plusDays(1);
        }

        log.info("[MoraCatchUp] Rango {}/{} → {} días hábiles | {} días omitidos",
                desde, hasta, diasHabiles, diasOmitidos);

        return diasHabiles;
    }
}