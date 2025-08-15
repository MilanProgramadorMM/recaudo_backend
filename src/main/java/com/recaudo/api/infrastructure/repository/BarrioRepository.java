package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.BarrioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarrioRepository extends JpaRepository<BarrioEntity, Long> {
    List<BarrioEntity> findByIdMunicipioAndStatusTrueOrderByValueAsc(Long idMunicipio);
}
