package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.OtherDiscountsGateway;
import com.recaudo.api.domain.gateway.TaxTypeGateway;
import com.recaudo.api.domain.model.dto.response.OtherDiscountsResponseDto;
import com.recaudo.api.domain.model.dto.response.TaxTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.OtherDiscountsCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.TaxTypeCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class OtherDiscountsUseCase {

    private OtherDiscountsGateway otherDiscountsGateway;


    public List<OtherDiscountsResponseDto> getAll() {
        return otherDiscountsGateway.getAll();
    }

    public OtherDiscountsResponseDto getById(Long id) {
        return otherDiscountsGateway.getById(id);
    }

    public OtherDiscountsResponseDto create(OtherDiscountsCreateDto data) {
        return otherDiscountsGateway.create(data);
    }

    public OtherDiscountsResponseDto edit(Long id, OtherDiscountsCreateDto data) {
        return otherDiscountsGateway.edit(id, data);
    }

    public OtherDiscountsResponseDto delete(Long id) {
        return otherDiscountsGateway.delete(id);
    }

}
