package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ZonaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaRepository extends JpaRepository<ZonaEntity, Long> {

    ZonaEntity findByValue(String value);
    boolean existsByValueIgnoreCaseAndIdNot(String value, Long id);
    List<ZonaEntity> findAllByStatusTrue(Sort sort);

}
