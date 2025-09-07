package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.DocumentTypeGateway;
import com.recaudo.api.domain.model.dto.response.DocumentTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DocumentTypeCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class DocumentTypeUseCase {

    private DocumentTypeGateway documentTypeGateway;


    public List<DocumentTypeResponseDto> getAll() {
        return documentTypeGateway.getAll();
    }

    public DocumentTypeResponseDto getById(Long id) {
        return documentTypeGateway.getById(id);
    }

    public DocumentTypeResponseDto create(DocumentTypeCreateDto data) {
        return documentTypeGateway.create(data);
    }

    public DocumentTypeResponseDto edit(Long id, DocumentTypeCreateDto data) {
        return documentTypeGateway.edit(id, data);
    }

    public DocumentTypeResponseDto delete(Long id) {
        return documentTypeGateway.delete(id);
    }

}
