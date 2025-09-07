package com.recaudo.api.infrastructure.adapter;


import com.recaudo.api.domain.gateway.OtherDiscountsGateway;
import com.recaudo.api.domain.gateway.TaxTypeGateway;
import com.recaudo.api.domain.model.dto.response.OtherDiscountsResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.OtherDiscountsCreateDto;
import com.recaudo.api.domain.model.entity.OtherDiscountsEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.OtherDiscountsRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OtherDiscountsAdapter implements OtherDiscountsGateway {

    OtherDiscountsRepository otherDiscountsRepository;

     @Override
     public List<OtherDiscountsResponseDto> getAll() {
         return otherDiscountsRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(data -> OtherDiscountsResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public OtherDiscountsResponseDto getById(Long id){
        return otherDiscountsRepository.findById(id)
                .map(data -> OtherDiscountsResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .orElseThrow(() -> new BadRequestException("Tipo de descuento con id " + id + " no encontrado"));
    }

    @Override
    public OtherDiscountsResponseDto create(OtherDiscountsCreateDto data) {
        if (otherDiscountsRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de descuento con este nombre");

        OtherDiscountsEntity entity = OtherDiscountsEntity.builder()
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .procedureName(data.getProcedure().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();

        otherDiscountsRepository.save(entity);

        return OtherDiscountsResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status("ACTIVO")
                .build();
    }

    @Override
    public OtherDiscountsResponseDto edit(Long id, OtherDiscountsCreateDto data) {
        OtherDiscountsEntity entity = otherDiscountsRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de descuento con id " + id + " no encontrado"));

        if (!entity.getName().equals(data.getName()) && otherDiscountsRepository.existsByName(data.getName())) {
            throw new BadRequestException("Ya existe un tipo de descuento con este nombre");
        }

        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setProcedureName(data.getProcedure().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        otherDiscountsRepository.save(entity);

        return OtherDiscountsResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status(entity.getDeletedAt() == null ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public OtherDiscountsResponseDto delete(Long id) {
        OtherDiscountsEntity entity = otherDiscountsRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de descuento con id " + id + " no encontrado"));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus(false);
        entity.setUserDelete(getUsernameToken());

        otherDiscountsRepository.save(entity);

        return OtherDiscountsResponseDto.builder()
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
