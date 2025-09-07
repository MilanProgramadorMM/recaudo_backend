package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ServiceQuotaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceQuotaRepository extends JpaRepository<ServiceQuotaEntity, Long> {
    boolean existsByName(String name);
    List<ServiceQuotaEntity> findAllByStatusTrue(Sort sort);

}
