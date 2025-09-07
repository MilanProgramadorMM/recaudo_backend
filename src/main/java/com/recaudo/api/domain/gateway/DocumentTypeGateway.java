package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.DocumentTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DocumentTypeCreateDto;

import java.util.List;

public interface DocumentTypeGateway {

    List<DocumentTypeResponseDto> getAll();
    DocumentTypeResponseDto getById(Long id);
    DocumentTypeResponseDto create(DocumentTypeCreateDto data);
    DocumentTypeResponseDto edit(Long id, DocumentTypeCreateDto data);
    DocumentTypeResponseDto delete(Long id);

}
