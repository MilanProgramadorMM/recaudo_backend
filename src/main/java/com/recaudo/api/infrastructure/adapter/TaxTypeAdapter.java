package com.recaudo.api.infrastructure.adapter;


import com.recaudo.api.domain.gateway.TaxTypeGateway;
import com.recaudo.api.domain.model.dto.response.TaxTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.TaxTypeCreateDto;
import com.recaudo.api.domain.model.entity.TaxTypeEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.TaxTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TaxTypeAdapter implements TaxTypeGateway {

    TaxTypeRepository taxTypeRepository;

     @Override
     public List<TaxTypeResponseDto> getAll() {
        return taxTypeRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(data -> TaxTypeResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public TaxTypeResponseDto getById(Long id){
        return taxTypeRepository.findById(id)
                .map(data -> TaxTypeResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .orElseThrow(() -> new BadRequestException("Tipo de tasa con id " + id + " no encontrado"));
    }

    @Override
    public TaxTypeResponseDto create(TaxTypeCreateDto data) {
        if (taxTypeRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de tasa con este nombre");

        TaxTypeEntity entity = TaxTypeEntity.builder()
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .procedureName(data.getProcedure().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();

        taxTypeRepository.save(entity);

        return TaxTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status("ACTIVO")
                .build();
    }

    @Override
    public TaxTypeResponseDto edit(Long id, TaxTypeCreateDto data) {
        TaxTypeEntity entity = taxTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de tasa con id " + id + " no encontrado"));

        if (!entity.getName().equals(data.getName()) && taxTypeRepository.existsByName(data.getName())) {
            throw new BadRequestException("Ya existe un tipo de tasa con este nombre");
        }

        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setProcedureName(data.getProcedure().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        taxTypeRepository.save(entity);

        return TaxTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status(entity.getDeletedAt() == null ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public TaxTypeResponseDto delete(Long id) {
        TaxTypeEntity entity = taxTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de tasa con id " + id + " no encontrado"));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus(false);
        entity.setUserDelete(getUsernameToken());

        taxTypeRepository.save(entity);

        return TaxTypeResponseDto.builder()
                .id(entity.getId())
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
