package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.ClosingSpendResponseDto;
import com.recaudo.api.domain.model.entity.ClosingSpendEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClosingSpendMapper {
    
    public ClosingSpendResponseDto toDto(ClosingSpendEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return ClosingSpendResponseDto.builder()
                .id(entity.getId())
                .closingId(entity.getClosingId())
                .spendTypeId(entity.getSpendTypeId())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .userCreate(entity.getUserCreate())
                .userEdit(entity.getUserEdit())
                .createdAt(String.valueOf(entity.getCreatedAt()))
                .editedAt(String.valueOf(entity.getEditedAt()))
                .status(entity.getStatus())
                .build();
    }
    
    public List<ClosingSpendResponseDto> toDtoList(List<ClosingSpendEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}