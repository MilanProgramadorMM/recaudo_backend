package com.recaudo.api.infrastructure.adapter;


import com.recaudo.api.domain.gateway.DocumentTypeGateway;
import com.recaudo.api.domain.model.dto.response.DocumentTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DocumentTypeCreateDto;
import com.recaudo.api.domain.model.entity.DocumentTypeEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.DocumentTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class DocumentTypeAdapter implements DocumentTypeGateway {

    DocumentTypeRepository documentTypeRepository;

     @Override
     public List<DocumentTypeResponseDto> getAll() {
         return documentTypeRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(data -> DocumentTypeResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public DocumentTypeResponseDto getById(Long id){
        return documentTypeRepository.findById(id)
                .map(data -> DocumentTypeResponseDto.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .description(data.getDescription())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .orElseThrow(() -> new BadRequestException("Tipo de documento con id " + id + " no encontrado"));
    }

    @Override
    public DocumentTypeResponseDto create(DocumentTypeCreateDto data) {
        if (documentTypeRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de documento con este nombre");

        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();

        documentTypeRepository.save(entity);

        return DocumentTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status("ACTIVO")
                .build();
    }

    @Override
    public DocumentTypeResponseDto edit(Long id, DocumentTypeCreateDto data) {
        DocumentTypeEntity entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de documento con id " + id + " no encontrado"));

        if (!entity.getName().equals(data.getName()) && documentTypeRepository.existsByName(data.getName())) {
            throw new BadRequestException("Ya existe un tipo de documento con este nombre");
        }

        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        documentTypeRepository.save(entity);

        return DocumentTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getDeletedAt() == null ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public DocumentTypeResponseDto delete(Long id) {
        DocumentTypeEntity entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Tipo de documento con id " + id + " no encontrado"));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus(false);
        entity.setUserDelete(getUsernameToken());

        documentTypeRepository.save(entity);

        return DocumentTypeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
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
