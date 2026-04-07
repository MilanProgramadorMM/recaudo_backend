package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CreditLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditLineRepository extends JpaRepository<CreditLineEntity, Long> {

    List<CreditLineEntity> findByLoanDisbursementIsTrue();

}
