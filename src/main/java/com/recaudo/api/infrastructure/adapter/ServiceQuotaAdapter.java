package com.recaudo.api.infrastructure.adapter;


import com.recaudo.api.domain.gateway.ServiceQuotaGateway;
import com.recaudo.api.domain.model.dto.response.ServiceQuotaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ServiceQuotaCreateDto;
import com.recaudo.api.domain.model.entity.ServiceQuotaEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.ServiceQuotaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ServiceQuotaAdapter implements ServiceQuotaGateway {

    ServiceQuotaRepository serviceQuotaRepository;

     @Override
     public List<ServiceQuotaResponseDto> getAll() {
         return serviceQuotaRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(data -> ServiceQuotaResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public ServiceQuotaResponseDto getById(Long id){
        return serviceQuotaRepository.findById(id)
                .map(data -> ServiceQuotaResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .orElseThrow(() -> new BadRequestException("Tipo de cargo por servicio con id " + id + " no encontrado"));
    }

    @Override
    public ServiceQuotaResponseDto create(ServiceQuotaCreateDto data) {
        if (serviceQuotaRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de cargo por servicio con este nombre");

        ServiceQuotaEntity entity = ServiceQuotaEntity.builder()
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .procedureName(data.getProcedure().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();

        serviceQuotaRepository.save(entity);

        return ServiceQuotaResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status("ACTIVO")
                .build();
    }

    @Override
    public ServiceQuotaResponseDto edit(Long id, ServiceQuotaCreateDto data) {
        ServiceQuotaEntity entity = serviceQuotaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de cargo por servicio con id " + id + " no encontrado"));

        if (!entity.getName().equals(data.getName()) && serviceQuotaRepository.existsByName(data.getName())) {
            throw new BadRequestException("Ya existe un tipo de cargo por servicio con este nombre");
        }

        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setProcedureName(data.getProcedure().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        serviceQuotaRepository.save(entity);

        return ServiceQuotaResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status(entity.getDeletedAt() == null ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public ServiceQuotaResponseDto delete(Long id) {
        ServiceQuotaEntity entity = serviceQuotaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de cargo por servicio con id " + id + " no encontrado"));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus(false);
        entity.setUserDelete(getUsernameToken());

        serviceQuotaRepository.save(entity);

        return ServiceQuotaResponseDto.builder()
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
