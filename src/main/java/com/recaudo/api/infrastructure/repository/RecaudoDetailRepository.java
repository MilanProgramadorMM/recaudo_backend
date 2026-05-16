package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.RecaudoDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RecaudoDetailRepository extends JpaRepository<RecaudoDetailEntity, Long> {
 
    List<RecaudoDetailEntity> findByRecaudoId(Long recaudoId);

    // RecaudoDetailRepository (new_recaudo_detail)
    List<RecaudoDetailEntity> findByRecaudoIdIn(List<Long> recaudoIds);
 
    @Query(value = """
        SELECT COALESCE(SUM(rd.value), 0)
        FROM new_recaudo_detail rd
        INNER JOIN new_recaudo r ON r.id = rd.recaudo_id
        WHERE r.quota_id = :quotaId
          AND rd.concept_id = :conceptId
    """, nativeQuery = true)
    BigDecimal sumByQuotaIdAndConceptId(
            @Param("quotaId") Long quotaId,
            @Param("conceptId") Long conceptId
    );
 
    @Query(value = """
        SELECT rd.*
        FROM new_recaudo_detail rd
        INNER JOIN new_recaudo r ON r.id = rd.recaudo_id
        WHERE r.quota_id = :quotaId
    """, nativeQuery = true)
    List<RecaudoDetailEntity> findAllByQuotaId(@Param("quotaId") Long quotaId);
}
 