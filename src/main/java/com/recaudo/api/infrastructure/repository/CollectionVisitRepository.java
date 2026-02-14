package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.CollectionVisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CollectionVisitRepository extends JpaRepository<CollectionVisitEntity, Long> {

    Optional<CollectionVisitEntity>
    findByCuotaIdAndVisitDate(Long cuotaId, LocalDate visitDate);

}
