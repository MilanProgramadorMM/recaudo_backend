package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DepartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<DepartamentoEntity, Long> {
    List<DepartamentoEntity> findByIdPaisAndStatusTrueOrderByValueAsc(Long idPais);
}
