package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.OtherDiscountsEntity;
import com.recaudo.api.domain.model.entity.TaxTypeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxTypeRepository extends JpaRepository<TaxTypeEntity, Long> {
    boolean existsByName(String name);

    List<TaxTypeEntity> findAllByStatusTrue(Sort sort);


}
