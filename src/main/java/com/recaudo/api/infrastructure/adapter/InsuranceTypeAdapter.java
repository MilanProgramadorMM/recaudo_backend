package com.recaudo.api.infrastructure.adapter;


import com.recaudo.api.domain.gateway.InsuranceTypeGateway;
import com.recaudo.api.domain.gateway.ServiceQuotaGateway;
import com.recaudo.api.domain.model.dto.response.InsuranceTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.InsuranceTypeCreateDto;
import com.recaudo.api.domain.model.entity.InsuranceTypeEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.InsuranceTypeRepository;
import com.recaudo.api.infrastructure.repository.ServiceQuotaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class InsuranceTypeAdapter implements InsuranceTypeGateway {

    InsuranceTypeRepository insuranceTypeRepository;

     @Override
     public List<InsuranceTypeResponseDto> getAll() {
         return insuranceTypeRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(data -> InsuranceTypeResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public InsuranceTypeResponseDto getById(Long id){
        return insuranceTypeRepository.findById(id)
                .map(data -> InsuranceTypeResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .orElseThrow(() -> new BadRequestException("Tipo de seguro con id " + id + " no encontrado"));
    }

    @Override
    public InsuranceTypeResponseDto create(InsuranceTypeCreateDto data) {
        if (insuranceTypeRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de seguro con este nombre");

        InsuranceTypeEntity entity = InsuranceTypeEntity.builder()
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .procedureName(data.getProcedure().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();

        insuranceTypeRepository.save(entity);

        return InsuranceTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status("ACTIVO")
                .build();
    }

    @Override
    public InsuranceTypeResponseDto edit(Long id, InsuranceTypeCreateDto data) {
        InsuranceTypeEntity entity = insuranceTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de seguro con id " + id + " no encontrado"));

        if (!entity.getName().equals(data.getName()) && insuranceTypeRepository.existsByName(data.getName())) {
            throw new BadRequestException("Ya existe un tipo de seguro con este nombre");
        }

        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setProcedureName(data.getProcedure().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        insuranceTypeRepository.save(entity);

        return InsuranceTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status(entity.getDeletedAt() == null ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public InsuranceTypeResponseDto delete(Long id) {
        InsuranceTypeEntity entity = insuranceTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de seguro con id " + id + " no encontrado"));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus(false);
        entity.setUserDelete(getUsernameToken());

        insuranceTypeRepository.save(entity);

        return InsuranceTypeResponseDto.builder()
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
