package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.GlotypesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlotypesRepository extends JpaRepository<GlotypesEntity, Long> {

    List<GlotypesEntity> findByKey(String key);
    Optional<GlotypesEntity> findByKeyAndCode(String key, String code);

}
