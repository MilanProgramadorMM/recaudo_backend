package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.AmortizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmortizationRepository extends JpaRepository<AmortizationEntity, Long> {
    List<AmortizationEntity> findByCreditId(Long creditId);
    void deleteByCreditId(Long creditId);
    /**
     * Obtiene todas las cuotas de un crédito con un estado de liquidación específico
     * ordenadas por número de cuota ascendente
     */
    List<AmortizationEntity> findByCreditIdAndLiquidatedOrderByQuotaNumberAsc(
            Long creditId,
            String liquidated
    );

    /**
     * Obtiene todas las cuotas de un crédito que están completamente pagadas
     * ordenadas por número de cuota ascendente
     */
    List<AmortizationEntity> findByCreditIdAndPaidFullOrderByQuotaNumberAsc(
            Long creditId,
            String paidFull
    );

    /**
     * Obtiene todas las cuotas de un crédito ordenadas por número de cuota
     */
    List<AmortizationEntity> findByCreditIdOrderByQuotaNumberAsc(Long creditId);

}