package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.BarrioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarrioRepository extends JpaRepository<BarrioEntity, Long> {

    List<BarrioEntity> findByIdMunicipioAndStatusTrueOrderByValueDesc(Long idMunicipio);
    boolean existsByValueIgnoreCaseAndIdMunicipio(String value, Long idMunicipio);
    boolean existsByValueIgnoreCaseAndIdMunicipioAndIdNot(String value, Long idMunicipio, Long id);


}
