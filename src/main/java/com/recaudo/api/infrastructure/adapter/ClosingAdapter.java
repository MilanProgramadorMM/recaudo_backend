package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.*;
import com.recaudo.api.domain.mapper.ClosingMapper;
import com.recaudo.api.domain.model.dto.response.ClosingResponseDto;
import com.recaudo.api.domain.model.dto.response.ClosingSpendResponseDto;
import com.recaudo.api.domain.model.dto.response.TodayClosingProjection;
import com.recaudo.api.domain.model.dto.rest_api.ClosingDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.ResourceNotFoundException;
import com.recaudo.api.domain.model.constant.ClosingStatus;
import com.recaudo.api.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class ClosingAdapter implements ClosingGateway {

    @Autowired
    ClosingRepository closingRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    ClosingStatusGateway closingStatusGateway;

    @Autowired
    GlotypesAdapter glotypesAdapter;

    @Autowired
    ClosingSpendGateway closingSpendGateway;

    @Autowired(required = false)
    ClosingMapper closingMapper = Mappers.getMapper(ClosingMapper.class);

    @Autowired
    ZonaGateway zonaGateway;


    @Override
    public List<ClosingResponseDto> getByPersonId(Long personId) {
        GlotypesEntity glotypesEntityBase
                = glotypesAdapter.getByCodeAndKey("TIPGAS", "BASE")
                    .orElse(null);
        if(glotypesEntityBase == null){
            throw new ResourceNotFoundException("Registro no encontrado");
        }

        List<Object[]> results = closingRepository.findClosingResume(personId);

        return results.stream()
                .map(row -> {
                    Long rowId = ((Number) row[0]).longValue();
                    ClosingSpendResponseDto dtoSpend =
                            closingSpendGateway.getSpendsByClosingAndType(rowId, glotypesEntityBase.getId());
                    ClosingResponseDto dto = new ClosingResponseDto();
                    dto.setId(rowId);
                    dto.setClosingDate(row[1] != null ? row[1].toString() : null);
                    dto.setObservation((String) row[2]);
                    dto.setUserCreate((String) row[3]);
                    dto.setNamePerson((String) row[4]);
                    dto.setCreatedAt(row[5] != null ? row[5].toString() : null);
                    dto.setClosingStatus(row[6] != null ? row[6].toString() : null);
                    dto.setAmount(dtoSpend != null ? dtoSpend.getAmount() : 0.0);
                    dto.setZona(row[7] != null ? row[7].toString() : null);
                    dto.setZonaId(row[8] != null ? Long.valueOf(row[8].toString()) : null);
                    return dto;
                })
                .toList();
    }

    @Override
    public ClosingResponseDto updateDeliveryAmounts(
            Long closingId,
            String deliveryType,
            Double amountAdmin,
            Double amountAsesor
    ) {
        ClosingEntity closing = closingRepository.findById(closingId)
                .orElseThrow(() -> new ResourceNotFoundException("Cierre no encontrado"));

        closing.setDeliveryType(deliveryType);
        closing.setAmountAdmin(amountAdmin);
        closing.setAmountAsesor(amountAsesor);
        closing.setEditedAt(LocalDateTime.now());

        ClosingEntity saved = closingRepository.save(closing);

        return closingMapper.entityToDto(saved);
    }


    @Override
    public ClosingResponseDto getById(Long id) {
        // Buscar la entidad por ID
        ClosingEntity entity = closingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cierre no encontrado: " + id));

        // Mapear entidad a DTO
        ClosingResponseDto dto = closingMapper.entityToDto(entity);

        // Obtener nombre de la persona
        personRepository.findById(entity.getPersonId())
                .ifPresent(person -> dto.setNamePerson(person.getFullName()));
        return dto;
    }

    @Override
    public Optional<TodayClosingProjection> getTodayClosingByPerson(Long personId) {
        LocalDate today = LocalDate.now();
        return closingRepository
                .findTodayClosing(personId, today);
    }

    @Override
    public Optional<TodayClosingProjection> getTodayClosingByPersonAndZona(
            Long personId,
            Long zonaId
    ) {
        return closingRepository.findTodayClosingByPersonAndZona(
                personId,
                zonaId,
                LocalDate.now()
        );
    }


    @Override
    public ClosingResponseDto save(ClosingDto dto) {

        validateDateToday(String.valueOf(dto.getClosingDate()));
        PersonEntity person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new BadRequestException("persona no existe: " + dto.getPersonId()));

        ZonaEntity zona = zonaGateway.getById(dto.getZonaId())
                .orElseThrow(() -> new BadRequestException("zona no existe: " + dto.getZonaId()));

        if (closingRepository.existsByPersonIdAndZonaIdAndClosingDate(
                dto.getPersonId(),
                dto.getZonaId(),
                LocalDate.parse(dto.getClosingDate())
        )) {
            throw new BadRequestException("Ya existe un cierre para este asesor y zona hoy");
        }


        ClosingEntity entity = ClosingEntity.builder()
                .closingDate(LocalDate.parse(dto.getClosingDate()))
                .personId(person.getId())
                .observation(dto.getObservation())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .status(true)
                .zonaId(zona.getId())
                .build();


        ClosingEntity saved = closingRepository.save(entity);
        closingStatusGateway.create(saved.getId(), saved.getUserCreate(), ClosingStatus.PRE_CIERRE );
        return closingMapper.entityToDto(saved);
    }

    @Override
    @Transactional
    public ClosingResponseDto edit(Long id, ClosingDto  dto) {
        validateDateToday(String.valueOf(dto.getClosingDate()));
        ClosingEntity entity = closingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cierre no encontrado: " + id));

        PersonEntity person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new BadRequestException("persona no existe: " + dto.getPersonId()));

        ZonaEntity zona = zonaGateway.getById(dto.getZonaId())
                .orElseThrow(() -> new BadRequestException("zona no existe: " + dto.getZonaId()));


                entity.setClosingDate(LocalDate.parse(dto.getClosingDate()));
                entity.setPersonId(dto.getPersonId());
                entity.setObservation(dto.getObservation());
                entity.setEditedAt(LocalDateTime.now());
                entity.setUserEdit(getUsernameToken());
                entity.setZonaId(zona.getId());

        ClosingEntity updated = closingRepository.save(entity);

        return closingMapper.entityToDto(updated);
    }

    private void validateDateToday(String date) {

        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("La fecha de cierre es obligatoria");
        }

        LocalDate closingDate;
        try {
            closingDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use yyyy-MM-dd");
        }

        LocalDate today = LocalDate.now();

        if (!closingDate.isEqual(today)) {
            throw new IllegalArgumentException(
                    "El cierre solo puede registrarse para la fecha actual (" + today + ")"
            );
        }
    }


    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

}
