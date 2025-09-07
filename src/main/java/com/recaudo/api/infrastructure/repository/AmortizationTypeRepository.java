package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.AmortizationTypeEntity;
import com.recaudo.api.domain.model.entity.TaxTypeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmortizationTypeRepository extends JpaRepository<AmortizationTypeEntity, Long> {

    boolean existsById(Long id);
    boolean existsByCode(String code);
    boolean existsByName(String name);
    List<AmortizationTypeEntity> findAllByStatusTrue(Sort sort);

}
