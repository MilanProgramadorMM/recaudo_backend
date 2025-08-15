package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, Integer> {
    List<ModuleEntity> findByType(Integer type); // 1 = TITLE, 2 = MODULE, 3 = SUB MODULE
    List<ModuleEntity> findByStatus(String status);
    List<ModuleEntity> findByStatusAndType(String status, Integer type);

}
