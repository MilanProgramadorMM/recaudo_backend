package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditLineGateway;
import com.recaudo.api.domain.model.dto.response.CreditLineResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditLineCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@UseCase
public class CreditLineUseCase {

    private CreditLineGateway creditLineGateway;

    public List<CreditLineResponseDto> getAll() {
        return creditLineGateway.get();
    }

    public CreditLineResponseDto register(CreditLineCreateDto data) {
        return creditLineGateway.create(data);
    }

    public CreditLineResponseDto update(Long id,CreditLineCreateDto data) {
        return creditLineGateway.update(id,data);
    }

    public CreditLineResponseDto delete(Long id){
        return creditLineGateway.delete(id);
    }
}
