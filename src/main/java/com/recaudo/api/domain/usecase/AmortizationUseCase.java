package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.AmortizationGateway;
import com.recaudo.api.domain.gateway.PeriodGateway;
import com.recaudo.api.domain.model.dto.response.AmortizationResponseDto;
import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class AmortizationUseCase {

    private AmortizationGateway amortizationGateway;


    public List<AmortizationResponseDto> getAll() {
        return amortizationGateway.getAll();
    }

}
