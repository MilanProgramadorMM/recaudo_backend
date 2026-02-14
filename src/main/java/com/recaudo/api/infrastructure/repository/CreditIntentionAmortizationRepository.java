package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditIntentionAmortizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditIntentionAmortizationRepository extends JpaRepository<CreditIntentionAmortizationEntity, Long> {

    @Modifying
    @Query("""
        DELETE FROM CreditIntentionAmortizationEntity a
        WHERE a.creditIntencionId = :creditIntencionId
    """)
    void deleteByCreditIntentionId(@Param("creditIntencionId") Long creditIntencionId);

    List<CreditIntentionAmortizationEntity> findByCreditIntencionId(Long creditIntencionId);

}
