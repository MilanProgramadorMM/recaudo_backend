package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Valores de los componentes de una cuota extraídos de credit_amortization_detail.
 * Evita requerir joins repetidos en el servicio.
 */
@Data
@Builder
public class QuotaComponentsDto {

    // ── Valores esperados (credit_amortization_detail) ────────────────────────
    private BigDecimal capital;
    private BigDecimal interest;
    private BigDecimal lifeInsurance;
    private BigDecimal portfolioInsurance;

    /**
     * Mora total causada hasta hoy para esta cuota.
     * Fuente: SUM(value) de credit_other_concepts_detail
     *         filtrado por amortization_id + glotype IMT.
     */
    private BigDecimal moraCausada;

    // ── Valores ya abonados ───────────────────────────────────────────────────
    private BigDecimal capitalPaid;
    private BigDecimal interestPaid;
    private BigDecimal lifeInsurancePaid;
    private BigDecimal portfolioInsurancePaid;

    /**
     * Mora ya pagada por el cliente.
     * Fuente: SUM(value).abs() de new_recaudo_detail
     *         filtrado por quota_id + glotype IMT.
     */
    private BigDecimal moraPaid;

    /** Valor nominal total de la cuota (credit_amortization_n.total_quota_value). */
    private BigDecimal totalQuotaValue;

    // ── Derivados ─────────────────────────────────────────────────────────────

    /**
     * Total ya abonado considerando todos los conceptos:
     * capital + interés + seguros + mora pagada.
     */
    public BigDecimal totalPaid() {
        return safe(capitalPaid)
                .add(safe(interestPaid))
                .add(safe(lifeInsurancePaid))
                .add(safe(portfolioInsurancePaid))
                .add(safe(moraPaid));
    }

    /**
     * Deuda total real del cliente = valor nominal de la cuota + mora causada.
     * Es contra este valor que se compara totalPaid() para marcar paid_full.
     */
    public BigDecimal totalDebt() {
        return safe(totalQuotaValue).add(safe(moraCausada));
    }

    /**
     * Saldo pendiente real = totalDebt - totalPaid.
     * Nunca negativo.
     */
    public BigDecimal totalPending() {
        BigDecimal pending = totalDebt().subtract(totalPaid());  // <-- FIX: era totalQuotaValue, debe ser totalDebt()
        return pending.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pending;
    }

    // ── Pendientes por componente (para distribución de pago) ─────────────────

    public BigDecimal pendingPortfolioInsurance() {
        return safe(portfolioInsurance).subtract(safe(portfolioInsurancePaid))
                .max(BigDecimal.ZERO);
    }

    public BigDecimal pendingLifeInsurance() {
        return safe(lifeInsurance).subtract(safe(lifeInsurancePaid))
                .max(BigDecimal.ZERO);
    }

    public BigDecimal pendingInterest() {
        return safe(interest).subtract(safe(interestPaid))
                .max(BigDecimal.ZERO);
    }

    public BigDecimal pendingCapital() {
        return safe(capital).subtract(safe(capitalPaid))
                .max(BigDecimal.ZERO);
    }

    public BigDecimal pendingMora() {
        return safe(moraCausada).subtract(safe(moraPaid))
                .max(BigDecimal.ZERO);
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}