package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.ClosingStatusGateway;
import com.recaudo.api.domain.mapper.ClosingStatusMapper;
import com.recaudo.api.domain.model.dto.response.ClosingStatusResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeClosingStatusDto;
import com.recaudo.api.domain.model.entity.ClosingStatusEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.CreditSimulationException;
import com.recaudo.api.domain.model.constant.ClosingStatus;
import com.recaudo.api.infrastructure.repository.ClosingRepository;
import com.recaudo.api.infrastructure.repository.ClosingStatusRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ClosingStatusAdapter implements ClosingStatusGateway {

    @Autowired
    private ClosingRepository closingRepository;

    @Autowired
    private ClosingStatusRepository closingStatusRepository;


    @Autowired(required = false)
    ClosingStatusMapper closingStatusMapper = Mappers.getMapper(ClosingStatusMapper.class);


    @Transactional
    @Override
    public ClosingStatusResponseDto create(Long closingId, String userStart, ClosingStatus code){

        if (closingId == null) {
            throw new IllegalArgumentException("Id de cierre es obligatorio");
        }

        if (!closingRepository.existsById(closingId)) {
            throw new BadRequestException("Cierre no existe");
        }


        if (userStart == null || userStart.isBlank()) {
            throw new IllegalArgumentException("Usuario que crea es obligatorio");
        }

        ClosingStatusEntity entitysaved;

        try {
            ClosingStatusEntity entity = new ClosingStatusEntity();
            entity.setClosingId(closingId);
            entity.setClosingStatus(code);
            entity.setUserStart(userStart);
            entity.setStatus(true);
            entity.setStartDate(LocalDate.now());

            entitysaved = closingStatusRepository.save(entity);

        } catch (DataIntegrityViolationException ex) {
            throw new CreditSimulationException(
                    "Error de integridad al crear el estado del cierre", ex
            );
        }

        return  closingStatusMapper.entityToDto(entitysaved);
    }

    @Transactional
    @Override
    public ClosingStatusResponseDto updateStatus(ChangeClosingStatusDto dto) {

        ClosingStatusEntity lastEntity =
                closingStatusRepository
                        .findTopByClosingIdAndStatusTrueOrderByStartDateDesc(dto.getClosingId())
                        .orElseThrow(() ->
                                new BadRequestException("El cierre no tiene estado"));


        ClosingStatus lastStatus = lastEntity.getClosingStatus();
        validateTransition(lastStatus , dto.getNewStatus());

        lastEntity.setStatus(false);
        lastEntity.setEndDate(LocalDate.now());
        lastEntity.setUserEnd(getUsernameToken());

        closingStatusRepository.save(lastEntity);

        ClosingStatusEntity entity = new ClosingStatusEntity();
        entity.setClosingId(dto.getClosingId());
        entity.setClosingStatus(dto.getNewStatus());
        entity.setUserStart(getUsernameToken());
        entity.setStatus(true);
        entity.setStartDate(LocalDate.now());

        ClosingStatusEntity entitySaved =  closingStatusRepository.save(entity);

        return closingStatusMapper.entityToDto(entitySaved);
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    private void validateTransition(ClosingStatus current, ClosingStatus next) {

        // Si ya finalizó, no se mueve más
        if (current == ClosingStatus.APPROVED || current == ClosingStatus.REJECTED) {
            throw new BadRequestException("El cierre ya finalizó su flujo");
        }

        // RECHAZED es válido desde cualquier estado
        if (next == ClosingStatus.REJECTED) {
            return;
        }

        // Flujo normal
        if (current == ClosingStatus.PRE_CIERRE && next != ClosingStatus.STUDY) {
            throw new BadRequestException("Solo se puede pasar de PRE_CIERRE a STUDY");
        }

        if (current == ClosingStatus.STUDY && next != ClosingStatus.PRE_APPROVED) {
            throw new BadRequestException("Solo se puede pasar de PRE_CIERRE a STUDY");
        }

        if (current == ClosingStatus.PRE_APPROVED && next != ClosingStatus.APPROVED) {
            throw new BadRequestException(
                    "Solo se puede pasar de STUDY a APPROVED"
            );
        }
    }

    @Override
    public ClosingStatusResponseDto getCurrentStatus(Long closingId) {
        ClosingStatusEntity entity = closingStatusRepository
                .findTopByClosingIdAndStatusTrueOrderByStartDateDesc(closingId)
                .orElseThrow(() ->
                        new BadRequestException("El cierre no tiene estados registrados")
                );

        // Obtener la zona
        Long zone = closingStatusRepository.findZoneByClosingId(closingId);

        ClosingStatusResponseDto dto = closingStatusMapper.entityToDto(entity);
        dto.setZone(zone);

        return dto;
    }

    @Override
    public List<ClosingStatusResponseDto> getStatusHistory(Long closingId) {

        List<ClosingStatusEntity> entities =
                closingStatusRepository.findByClosingIdOrderByStartDateAsc(closingId);

        if (entities.isEmpty()) {
            throw new BadRequestException("El cierre no tiene historial de estados");
        }

        return entities.stream()
                .map(closingStatusMapper::entityToDto)
                .toList();
    }




}
