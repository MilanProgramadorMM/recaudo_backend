package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DetalleDelayPenaltyAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleDelayPenaltyAgreementRepository extends JpaRepository<DetalleDelayPenaltyAgreementEntity, Long> {
    List<DetalleDelayPenaltyAgreementEntity> findByAgreementId(Long agreementId);

    @Query("SELECT d.cuotaId FROM DetalleDelayPenaltyAgreementEntity d " +
            "INNER JOIN DelayPenaltyAgreementEntity m ON m.id = d.agreement.id " +
            "WHERE m.creditId = :creditId AND m.status = false")
    List<Long> findPactedCuotaIdsByCreditId(@Param("creditId") Long creditId);
}