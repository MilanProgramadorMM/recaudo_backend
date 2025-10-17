package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PersonZonaGateway;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenPerson;
import com.recaudo.api.domain.model.entity.PersonZonaEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.PersonZonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class PersonZonaAdapter implements PersonZonaGateway {

    @Autowired
    private PersonZonaRepository personzonaRepository;

    @Override
    public void updateOrdenClientes(UpdateOrdenList list) {
        for (UpdateOrdenPerson cliente : list.getClientes()) {
            personzonaRepository.findByPersonIdAndStatusTrue(cliente.getPersonId())
                    .ifPresent(entity -> {
                        entity.setOrden(cliente.getOrden());
                        personzonaRepository.save(entity);
                    });
        }
    }


    @Override
    public void updateClientToZone(Long personId, Long zonaId) {
        // Buscar la relación actual del cliente
        PersonZonaEntity currentZone = personzonaRepository.findByPersonId(personId)
                .orElseThrow(() -> new BadRequestException("El cliente no está asignado a ninguna zona"));

        if (currentZone.getZonaId().equals(zonaId)) {
            return;
        }

        // Si la zona cambia
        // Desactivar el registro anterior (mantener histórico)
        currentZone.setStatus(false);
        currentZone.setEditedAt(LocalDateTime.now());
        personzonaRepository.save(currentZone);

        // Crear un nuevo registro con la nueva zona
        PersonZonaEntity newZone = PersonZonaEntity.builder()
                .personId(personId)
                .zonaId(zonaId)
                .orden(0L)
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        personzonaRepository.save(newZone);
    }

}
