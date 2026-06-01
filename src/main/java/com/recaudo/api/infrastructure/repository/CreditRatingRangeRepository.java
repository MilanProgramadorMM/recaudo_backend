package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditRatingRangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditRatingRangeRepository extends JpaRepository<CreditRatingRangeEntity, Integer> {

    @Query(value = """
        SELECT *
        FROM credit_rating_range
        WHERE start <= :dias
          AND (end IS NULL OR end >= :dias)
        """, nativeQuery = true)
    Optional<CreditRatingRangeEntity> findByDiasMora(@Param("dias") Integer dias);
}