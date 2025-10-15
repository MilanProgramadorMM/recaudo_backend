package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.InsuranceTypeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceTypeRepository extends JpaRepository<InsuranceTypeEntity, Long> {
    boolean existsByName(String name);
    List<InsuranceTypeEntity> findAllByStatusTrue(Sort sort);

}
