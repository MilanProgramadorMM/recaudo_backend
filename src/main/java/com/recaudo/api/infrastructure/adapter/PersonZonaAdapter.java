package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PersonZonaGateway;
import com.recaudo.api.domain.model.dto.response.AsesorZonaDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenPerson;
import com.recaudo.api.domain.model.entity.PersonZonaEntity;
import com.recaudo.api.domain.model.entity.ZonaEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.PersonRepository;
import com.recaudo.api.infrastructure.repository.PersonZonaRepository;
import com.recaudo.api.infrastructure.repository.ZonaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class PersonZonaAdapter implements PersonZonaGateway {

    @Autowired
    private PersonZonaRepository personzonaRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    // Metodo para asignar múltiples zonas a un ASESOR
    @Override
    @Transactional
    public void assignZonasToAsesor(Long personId, List<Long> zonasIds) {
        if (!personRepository.existsById(personId)) {
            throw new BadRequestException("No existe la persona para asignar zonas");
        }

        // 1. Desactivar todas las zonas actuales del asesor
        List<PersonZonaEntity> zonasActuales =
                personzonaRepository.findByPersonIdAndStatusTrueAndIsAsesorTrue(personId);

        zonasActuales.forEach(zona -> {
            zona.setStatus(false);
            zona.setEditedAt(LocalDateTime.now());
        });
        personzonaRepository.saveAll(zonasActuales);

        // 2. Crear nuevas asignaciones para cada zona
        List<PersonZonaEntity> nuevasAsignaciones = new ArrayList<>();

        for (Long zonaId : zonasIds) {
            nuevasAsignaciones.add(
                    PersonZonaEntity.builder()
                    .personId(personId)
                    .zonaId(zonaId)
                    .orden(0) // Los asesores no necesitan orden
                    .status(true)
                    .isAsesor(true)
                    .createdAt(LocalDateTime.now())
                    .build()
            );
        }

        personzonaRepository.saveAll(nuevasAsignaciones);
    }

    // Método para obtener las zonas de un ASESOR
    @Override
    public List<AsesorZonaDto> getZonasByAsesor(Long personId) {
        List<PersonZonaEntity> zonasAsesor =
                personzonaRepository.findByPersonIdAndStatusTrueAndIsAsesorTrue(personId);

        return zonasAsesor.stream()
                .map(pz -> {
                    ZonaEntity zona = zonaRepository.findById(pz.getZonaId())
                            .orElse(null);

                    return AsesorZonaDto.builder()
                            .id(pz.getId())
                            .zonaId(pz.getZonaId())
                            .zonaName(zona != null ? zona.getValue() : "Zona no encontrada")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOrdenClientes(UpdateOrdenList list) {
        if (list.getClientes() == null || list.getClientes().isEmpty()) {
            return;
        }

        // Obtener la zona del primer elemento
        Long zonaId = list.getClientes().get(0).getZonaId();

        if (zonaId != null) {
            // Resetear órdenes de la zona
            List<PersonZonaEntity> entidadesZona = personzonaRepository.findAllByZonaIdAndStatusTrue(zonaId);
            entidadesZona.forEach(entity -> entity.setOrden(0));
            personzonaRepository.saveAll(entidadesZona);
        }

        // Asignar los nuevos órdenes según la lista recibida
        for (UpdateOrdenPerson clienteDto : list.getClientes()) {
            personzonaRepository.findByPersonIdAndStatusTrueAndIsAsesorFalse(clienteDto.getPersonId())
                    .ifPresent(entity -> {
                        entity.setOrden(clienteDto.getOrden());
                        entity.setEditedAt(LocalDateTime.now());
                        personzonaRepository.save(entity);
                    });
        }
    }


    @Override
    @Transactional
    public void updateClientToZone(Long personId, Long zonaId) {

        if (!personRepository.existsById(personId)) {
            throw new BadRequestException("No existe la persona para asignar zona");
        }

        // 1. Buscar si ya tiene una zona activa
        Optional<PersonZonaEntity> currentZoneOpt =
                personzonaRepository.findByPersonIdAndStatusTrueAndIsAsesorFalse(personId);

        // Si ya está en esa zona, no hacer nada
        if (currentZoneOpt.isPresent() && currentZoneOpt.get().getZonaId().equals(zonaId)) {
            return;
        }

        // 2. Si tenía una zona anterior diferente, la desactivamos
        if (currentZoneOpt.isPresent()) {
            PersonZonaEntity currentZone = currentZoneOpt.get();
            currentZone.setStatus(false);
            currentZone.setEditedAt(LocalDateTime.now());
            personzonaRepository.save(currentZone);
        }

        // 3. Calcular el nuevo orden para la zona de destino (para que quede al final)
        int nuevoOrden = calcularSiguienteOrden(zonaId);

        // 4. Crear el nuevo registro en la nueva zona
        PersonZonaEntity newZone = PersonZonaEntity.builder()
                .personId(personId)
                .zonaId(zonaId)
                .orden(nuevoOrden) // <--- Se asigna el int correctamente
                .status(true)
                .isAsesor(false)
                .createdAt(LocalDateTime.now())
                .build();

        personzonaRepository.save(newZone);
    }

    private int calcularSiguienteOrden(Long zonaId) {
        List<PersonZonaEntity> clientesEnZona = personzonaRepository
                .findAllByZonaIdOrderByOrdenAsc(zonaId);

        if (clientesEnZona.isEmpty()) {
            return 1;
        }

        // Obtenemos el orden del último y sumamos 1
        return clientesEnZona.get(clientesEnZona.size() - 1).getOrden() + 1;
    }


}
