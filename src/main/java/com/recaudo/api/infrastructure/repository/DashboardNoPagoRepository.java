package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DashboardNoPagoEntity;
import com.recaudo.api.domain.model.entity.DashboardRecaudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardNoPagoRepository extends JpaRepository<DashboardNoPagoEntity, Long> {

}
