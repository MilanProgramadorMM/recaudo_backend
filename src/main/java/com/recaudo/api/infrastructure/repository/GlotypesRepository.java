package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.GlotypesProjection;
import com.recaudo.api.domain.model.entity.GlotypesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlotypesRepository extends JpaRepository<GlotypesEntity, Long> {

    @Query(value = "SELECT type, key, name, id FROM glotypes g WHERE g.key = :key", nativeQuery = true)
    List<GlotypesProjection> findByKey(String key);

    @Query(value = """
        SELECT type, key, name, id
        FROM glotypes g
        WHERE g.type = :type
    """, nativeQuery = true)
    List<GlotypesProjection> findByType(Long type);

}
