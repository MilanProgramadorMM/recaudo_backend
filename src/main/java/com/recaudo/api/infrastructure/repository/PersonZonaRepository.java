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

    // Para buscar por persona y zona específica (útil para CLIENTES)
    Optional<PersonZonaEntity> findByPersonIdAndZonaIdAndStatusTrue(Long personId, Long zonaId);

    // Para obtener todas las zonas activas de una persona (útil para ASESORES)
    List<PersonZonaEntity> findAllByPersonIdAndStatusTrue(Long personId);

    List<PersonZonaEntity> findAllByZonaIdOrderByOrdenAsc(Long zonaId);

    List<PersonZonaEntity> findAllByZonaIdAndStatusTrue(Long zonaId);


}

