package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.TaxTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.TaxTypeCreateDto;

import java.util.List;

public interface TaxTypeGateway {

    List<TaxTypeResponseDto> getAll();
    TaxTypeResponseDto getById(Long id);
    TaxTypeResponseDto create(TaxTypeCreateDto data);
    TaxTypeResponseDto edit(Long id, TaxTypeCreateDto data);
    TaxTypeResponseDto delete(Long id);

}
