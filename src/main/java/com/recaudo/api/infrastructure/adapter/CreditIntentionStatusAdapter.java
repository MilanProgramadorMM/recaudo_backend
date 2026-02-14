package com.recaudo.api.infrastructure.adapter;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recaudo.api.domain.gateway.CreditIntentionGateway;
import com.recaudo.api.domain.gateway.CreditIntentionStatusGateway;
import com.recaudo.api.domain.mapper.CreditIntentionMapper;
import com.recaudo.api.domain.mapper.CreditIntentionStatusMapper;
import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.IntentionCreditResponseAllDto;
import com.recaudo.api.domain.model.dto.response.ProyeccionAmortizacionDto;
import com.recaudo.api.domain.model.dto.rest_api.*;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.CreditSimulationException;
import com.recaudo.api.infrastructure.helper.util.CreditStatusCode;
import com.recaudo.api.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class CreditIntentionStatusAdapter implements CreditIntentionStatusGateway {

    @Autowired
    private CreditIntentionRepository creditIntentionRepository;

    @Autowired
    private  CreditIntentionStatusRepository creditIntentionStatusRepository;


    @Autowired(required = false)
    CreditIntentionStatusMapper creditIntentionMapper = Mappers.getMapper(CreditIntentionStatusMapper.class);


    @Transactional
    @Override
    public CreditIntentionStatusResponseDto create(Long creditId, String userStart, CreditStatusCode code){

        if (creditId == null) {
            throw new IllegalArgumentException("Id de intencion de credito es obligatorio");
        }

        if (!creditIntentionRepository.existsById(creditId)) {
            throw new BadRequestException("La intención de crédito no existe");
        }


        if (userStart == null || userStart.isBlank()) {
            throw new IllegalArgumentException("Usuario que crea es obligatorio");
        }

        CreditIntentionStatusEntity entitysaved;

        try {
            CreditIntentionStatusEntity entity = new CreditIntentionStatusEntity();
            entity.setCreditIntentionId(creditId);
            entity.setCode(code);
            entity.setUserStart(userStart);
            entity.setStatus(true);
            entity.setStartDate(LocalDateTime.now());

            entitysaved = creditIntentionStatusRepository.save(entity);

        } catch (DataIntegrityViolationException ex) {
            throw new CreditSimulationException(
                    "Error de integridad al crear el estado del crédito", ex
            );
        }

        return  creditIntentionMapper.entityToDto(entitysaved);
    }

    @Transactional
    @Override
    public CreditIntentionStatusResponseDto updateStatus(ChangeCreditStatusDto dto) {

        CreditIntentionStatusEntity lastEntity =
                creditIntentionStatusRepository
                        .findTopByCreditIntentionIdOrderByStartDateDesc(dto.getCreditId())
                        .orElseThrow(() ->
                                new BadRequestException("El crédito no tiene estado"));


        CreditStatusCode lastStatus = lastEntity.getCode();
        validateTransition(lastStatus , dto.getNewStatus());

        lastEntity.setStatus(false);
        lastEntity.setEndDate(LocalDateTime.now());
        lastEntity.setUserEnd(getUsernameToken());

        creditIntentionStatusRepository.save(lastEntity);

        CreditIntentionStatusEntity entity = new CreditIntentionStatusEntity();
        entity.setCreditIntentionId(dto.getCreditId());
        entity.setCode(dto.getNewStatus());
        entity.setUserStart(getUsernameToken());
        entity.setStatus(true);
        entity.setStartDate(LocalDateTime.now());

        CreditIntentionStatusEntity entitySaved =  creditIntentionStatusRepository.save(entity);

        return creditIntentionMapper.entityToDto(entitySaved);
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    private void validateTransition(CreditStatusCode current, CreditStatusCode next) {

        // Si ya finalizó, no se mueve más
        if (current == CreditStatusCode.TERMINATED || current == CreditStatusCode.RECHAZED) {
            throw new BadRequestException("El crédito ya finalizó su flujo");
        }

        // RECHAZED es válido desde cualquier estado
        if (next == CreditStatusCode.RECHAZED) {
            return;
        }

        // Flujo normal
        if (current == CreditStatusCode.STUDY && next != CreditStatusCode.APPROVED) {
            throw new BadRequestException("Solo se puede pasar de STUDY a APPROVED");
        }

        if (current == CreditStatusCode.APPROVED && next != CreditStatusCode.IMPROVEMENT) {
            throw new BadRequestException("Solo se puede pasar de APPROVED a IMPROVEMENT");
        }

        if (current == CreditStatusCode.IMPROVEMENT && next != CreditStatusCode.DISBURSEMENT) {
            throw new BadRequestException("Solo se puede pasar de IMPROVEMENT a DISBURSEMENT");
        }

        if (current == CreditStatusCode.DISBURSEMENT && next != CreditStatusCode.TERMINATED) {
            throw new BadRequestException(
                    "Solo se puede pasar de DISBURSEMENT a TERMINATED"
            );
        }
    }


}
