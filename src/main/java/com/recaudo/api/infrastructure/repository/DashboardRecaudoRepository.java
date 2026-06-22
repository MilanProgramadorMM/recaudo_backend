package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DashboardRecaudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DashboardRecaudoRepository extends JpaRepository<DashboardRecaudoEntity, Long> {

    Optional<DashboardRecaudoEntity> findByZonaIdAndCreateRecaudo(Long zonaId, LocalDate recaudoDate);
}
