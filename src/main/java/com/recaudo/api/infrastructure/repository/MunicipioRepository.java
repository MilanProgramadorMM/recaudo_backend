package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.MunicipioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MunicipioRepository extends JpaRepository<MunicipioEntity, Long> {
    List<MunicipioEntity> findByIdDepartamentoAndStatusTrueOrderByValueAsc(Long idDepartamento);
    boolean existsByValueIgnoreCaseAndIdDepartamento(String value, Long idDepartamento);
    boolean existsByValueIgnoreCaseAndIdDepartamentoAndIdNot(String value, Long idDepartamento, Long id);

}
