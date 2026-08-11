package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DashboardDebidoCobrarEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardDebidoCobrarRepository extends JpaRepository<DashboardDebidoCobrarEntity, Long> {

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM DashboardDebidoCobrarEntity d
        WHERE d.zonaId = :zonaId
          AND d.createdAt >= :inicio
          AND d.createdAt < :fin
    """)
    void deleteByZonaIdAndFecha(
            @Param("zonaId") Long zonaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("""
        SELECT d.value
        FROM DashboardDebidoCobrarEntity d
        WHERE d.zonaId = :zonaId
          AND d.createdAt >= :inicio
          AND d.createdAt < :fin
        ORDER BY d.createdAt DESC
    """)
    Optional<BigDecimal> findLatestValueByZonaIdAndFecha(
            @Param("zonaId") Long zonaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("""
    SELECT d
    FROM DashboardDebidoCobrarEntity d
    WHERE d.zonaId = :zonaId
      AND d.createdAt >= :inicio
      AND d.createdAt < :fin
    ORDER BY d.createdAt DESC
""")
    List<DashboardDebidoCobrarEntity> findLatestEntityByZonaIdAndFecha(
            @Param("zonaId") Long zonaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}
