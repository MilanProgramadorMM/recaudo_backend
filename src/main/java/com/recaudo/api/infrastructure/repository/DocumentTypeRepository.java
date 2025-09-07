package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.DocumentTypeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {

    boolean existsById(Long id);
    boolean existsByName(String name);
    List<DocumentTypeEntity> findAllByStatusTrue(Sort sort);

}
