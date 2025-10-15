package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.InsuranceTypeGateway;
import com.recaudo.api.domain.model.dto.response.InsuranceTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.InsuranceTypeCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class InsuranceTypeUseCase {

    private InsuranceTypeGateway insuranceGateway;


    public List<InsuranceTypeResponseDto> getAll() {
        return insuranceGateway.getAll();
    }

    public InsuranceTypeResponseDto getById(Long id) {
        return insuranceGateway.getById(id);
    }

    public InsuranceTypeResponseDto create(InsuranceTypeCreateDto data) {
        return insuranceGateway.create(data);
    }

    public InsuranceTypeResponseDto edit(Long id, InsuranceTypeCreateDto data) {
        return insuranceGateway.edit(id, data);
    }

    public InsuranceTypeResponseDto delete(Long id) {
        return insuranceGateway.delete(id);
    }

}
