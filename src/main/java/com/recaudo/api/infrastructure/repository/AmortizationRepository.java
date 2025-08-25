package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.AmortizationdEntity;
import com.recaudo.api.domain.model.entity.PeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmortizationRepository extends JpaRepository<AmortizationdEntity, Long> {
}
