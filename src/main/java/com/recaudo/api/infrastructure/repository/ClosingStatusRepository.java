package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ClosingStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClosingStatusRepository extends JpaRepository<ClosingStatusEntity, Long> {
    Optional<ClosingStatusEntity>
    findTopByClosingIdAndStatusTrueOrderByStartDateDesc(Long creditIntentionId);

    List<ClosingStatusEntity>
    findByClosingIdOrderByStartDateAsc(Long closingId);

    @Query(value = """ 
            SELECT c.zona_id zone FROM closing c
            INNER JOIN closing_status cs ON cs.closing_id = c.id 
            WHERE cs.closing_id = :closingId 
            AND cs.status = true 
            ORDER BY cs.start_date DESC LIMIT 1
            """,
            nativeQuery = true)
    Long findZoneByClosingId(@Param("closingId") Long closingId);
}
