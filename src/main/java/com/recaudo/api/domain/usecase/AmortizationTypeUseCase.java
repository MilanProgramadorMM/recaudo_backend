package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.AmortizationTypeGateway;
import com.recaudo.api.domain.model.dto.response.AmortizationTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.AmortizationTypeCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class AmortizationTypeUseCase {

    private AmortizationTypeGateway amortizationGateway;


    public List<AmortizationTypeResponseDto> getAll() {
        return amortizationGateway.getAll();
    }
    public AmortizationTypeResponseDto getById(Long id) {
        return amortizationGateway.getById(id);
    }

    public AmortizationTypeResponseDto create(AmortizationTypeCreateDto data) {
        return amortizationGateway.create(data);
    }

    public AmortizationTypeResponseDto edit(Long id, AmortizationTypeCreateDto data) {
        return amortizationGateway.edit(id, data);
    }

    public AmortizationTypeResponseDto delete(Long id) {
        return amortizationGateway.delete(id);
    }
}
