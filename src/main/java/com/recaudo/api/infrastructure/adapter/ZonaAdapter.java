package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.ZonaGateway;
import com.recaudo.api.domain.model.dto.response.ZonaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;
import com.recaudo.api.domain.model.entity.ZonaEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.ZonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ZonaAdapter implements ZonaGateway {

    @Autowired
    ZonaRepository zonaRepository;

    @Override
    public ZonaResponseDto create(ZonaCreateDto zonaCreateDto) {
        // Validar nombre único
        if (zonaRepository.findByValue(zonaCreateDto.getValue()) != null) {
            throw new BadRequestException("Ya existe una zona con el nombre: " + zonaCreateDto.getValue());
        }

        // Crear zona
        ZonaEntity entity = ZonaEntity.builder()
                .value(zonaCreateDto.getValue().toUpperCase())
                .description(zonaCreateDto.getDescription().toUpperCase())
                .status(true)
                .createdAt(LocalDateTime.now())
                .userCreate(getUsernameToken())
                .build();

        ZonaEntity saved = zonaRepository.save(entity);

        return ZonaResponseDto.builder()
                .id(saved.getId())
                .value(saved.getValue())
                .description(saved.getDescription())
                .build();
    }


    @Override
    public ZonaResponseDto update(Long id, ZonaCreateDto zonaCreateDto) {
        ZonaEntity zona = zonaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La zona no existe"));

        // Validar duplicado (ignorando mayúsculas y el mismo id)
        boolean exists = zonaRepository.existsByValueIgnoreCaseAndIdNot(zonaCreateDto.getValue(), id);
        if (exists) {
            throw new BadRequestException("Ya existe una zona con el nombre: " + zonaCreateDto.getValue());
        }

        zona.setValue(zonaCreateDto.getValue().toUpperCase());
        zona.setDescription(zonaCreateDto.getDescription().toUpperCase());
        zona.setEditedAt(LocalDateTime.now());
        zona.setUserEdit(getUsernameToken());

        ZonaEntity saved = zonaRepository.save(zona);

        return ZonaResponseDto.builder()
                .id(saved.getId())
                .value(saved.getValue())
                .description(saved.getDescription())
                .build();
    }

    //Todo: preguntar sobre validaciones a tener en cuenta antes de eliminar zona
    @Override
    public void delete(Long id) {
        ZonaEntity zona = zonaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La zona no existe"));

        // Borrado lógico
        zona.setStatus(false);
        zona.setDeletedAt(LocalDateTime.now());
        zona.setUserEdit(getUsernameToken());

        zonaRepository.save(zona);
    }

    @Override
    public List<ZonaResponseDto> getStatusTrue() {
        return zonaRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(z -> ZonaResponseDto.builder()
                        .id(z.getId())
                        .value(z.getValue())
                        .description(z.getDescription())
                        .status(z.isStatus()? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
    }

    @Override
    public List<ZonaResponseDto> getAll() {
        return zonaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(z -> ZonaResponseDto.builder()
                        .id(z.getId())
                        .value(z.getValue())
                        .description(z.getDescription())
                        .status(z.isStatus()? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}
