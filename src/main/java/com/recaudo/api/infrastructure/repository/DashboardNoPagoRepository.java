package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DashboardNoPagoEntity;
import com.recaudo.api.domain.model.entity.DashboardRecaudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DashboardNoPagoRepository extends JpaRepository<DashboardNoPagoEntity, Long> {

    Optional<DashboardNoPagoEntity> findByZonaIdAndCreateNopago(
            Long zonaId,
            LocalDate createNopago
    );

}
