package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.PaisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaisRepository extends JpaRepository<PaisEntity, Long> {
    List<PaisEntity> findAllByStatusTrueOrderByValueAsc();
}
