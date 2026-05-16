package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditAmortizationNEntity;
import com.recaudo.api.domain.model.entity.CreditIntentionAmortizationNEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditAmortizationNRepository extends JpaRepository<CreditAmortizationNEntity, Long> {
    List<CreditAmortizationNEntity> findByCreditIdOrderByQuotaNumber(Long creditId);

    @Modifying
    void deleteByCreditId(Long creditId);

    List<CreditAmortizationNEntity> findByCreditIdOrderByQuotaNumberAsc(Long creditId);

    List<CreditAmortizationNEntity> findByCreditIdAndPaidFullOrderByQuotaNumberAsc(
            Long creditId, String paidFull);

    /**
     * Retorna cuotas cuya fecha de vencimiento es <= hoy y no han sido pagadas.
     * El operador <= garantiza que las que vencen HOY también se incluyen,
     * ya que el job corre a las 11:30 PM y nadie pagará a esa hora.
     */
    @Query("SELECT c FROM CreditAmortizationNEntity c " +
            "WHERE c.expirationDate <= :fecha " +
            "AND c.paidFull = 'N'")
    List<CreditAmortizationNEntity> findOverdueQuotas(@Param("fecha") LocalDate fecha);



}