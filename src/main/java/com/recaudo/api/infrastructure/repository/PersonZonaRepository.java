package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.PersonZonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PersonZonaRepository extends JpaRepository<PersonZonaEntity, Long> {
    Optional<PersonZonaEntity> findByPersonId(Long personId);
    Optional<PersonZonaEntity> findByPersonIdAndStatusTrue(Long personId);

    List<PersonZonaEntity> findAllByZonaIdOrderByOrdenAsc(Long zonaId);

    // Trae todos los clientes de la zona cuyo orden sea mayor o igual al que vamos a insertar
   // List<PersonZonaEntity> findAllByZonaIdAndOrdenGreaterThanEqualOrderByOrdenAsc(Long zonaId, Long orden);


}

