package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.TaxTypeGateway;
import com.recaudo.api.domain.model.dto.response.TaxTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.TaxTypeCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class TaxTypeUseCase {

    private TaxTypeGateway taxTypeGateway;


    public List<TaxTypeResponseDto> getAll() {
        return taxTypeGateway.getAll();
    }

    public TaxTypeResponseDto getById(Long id) {
        return taxTypeGateway.getById(id);
    }

    public TaxTypeResponseDto create(TaxTypeCreateDto data) {
        return taxTypeGateway.create(data);
    }

    public TaxTypeResponseDto edit(Long id, TaxTypeCreateDto data) {
        return taxTypeGateway.edit(id, data);
    }

    public TaxTypeResponseDto delete(Long id) {
        return taxTypeGateway.delete(id);
    }

}
