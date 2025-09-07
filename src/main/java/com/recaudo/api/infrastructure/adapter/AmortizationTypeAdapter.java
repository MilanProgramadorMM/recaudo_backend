package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.AmortizationTypeGateway;
import com.recaudo.api.domain.model.dto.response.AmortizationTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.AmortizationTypeCreateDto;
import com.recaudo.api.domain.model.entity.AmortizationTypeEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.AmortizationTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AmortizationTypeAdapter implements AmortizationTypeGateway {

    AmortizationTypeRepository amortizationRepository;

     @Override
     public List<AmortizationTypeResponseDto> getAll() {
        return amortizationRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))                .stream()
                .map(data -> AmortizationTypeResponseDto.builder()
                        .id(data.getId())
                        .code(data.getCode())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public AmortizationTypeResponseDto getById(Long id) {
        return amortizationRepository.findById(id)
                .map(data -> AmortizationTypeResponseDto.builder()
                        .id(data.getId())
                        .code(data.getCode())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build()
                )
                .orElseThrow(() -> new BadRequestException("AmortizationType con id " + id + " no encontrado"));
    }

    @Override
    public AmortizationTypeResponseDto create(AmortizationTypeCreateDto data) {

        if (amortizationRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de amortizacion con este nombre");

         if(amortizationRepository.existsByCode(data.getCode()))
             throw new BadRequestException("Ya existe este codigo de amortizacion");

        AmortizationTypeEntity amortizationTypeEntity = AmortizationTypeEntity.builder()
                .code(data.getCode().toUpperCase())
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .procedureName(data.getProcedure().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();
        amortizationRepository.save(amortizationTypeEntity);

         return AmortizationTypeResponseDto.builder()
                 .id(amortizationTypeEntity.getId())
                 .code(amortizationTypeEntity.getCode())
                 .description(amortizationTypeEntity.getDescription())
                 .procedure(amortizationTypeEntity.getProcedureName())
                 .status(amortizationTypeEntity.isStatus() ? "ACTIVO" : "INACTIVO")
                 .build();
    }

    @Override
    public AmortizationTypeResponseDto edit(Long id, AmortizationTypeCreateDto data) {
        AmortizationTypeEntity entity = amortizationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("AmortizationType con id " + id + " no encontrado"));

        // validación si el nuevo código ya existe en otro registro
        if (!entity.getCode().equals(data.getCode()) && amortizationRepository.existsByCode(data.getCode())) {
            throw new BadRequestException("Ya existe un tipo de amortización con este código");
        }

        entity.setCode(data.getCode().toUpperCase());
        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setProcedureName(data.getProcedure().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        amortizationRepository.save(entity);

        return AmortizationTypeResponseDto.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status(entity.isStatus() ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public AmortizationTypeResponseDto delete(Long id) {
        AmortizationTypeEntity entity = amortizationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("AmortizationType con id " + id + " no encontrado"));

        // Eliminación lógica
        entity.setStatus(false);
        entity.setUserEdit(getUsernameToken());
        entity.setDeletedAt(LocalDateTime.now());

        amortizationRepository.save(entity);

        return AmortizationTypeResponseDto.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status("INACTIVO")
                .build();
    }


    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

}
