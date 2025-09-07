package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.AmortizationTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.AmortizationTypeCreateDto;

import java.util.List;

public interface AmortizationTypeGateway {

    List<AmortizationTypeResponseDto> getAll();
    AmortizationTypeResponseDto getById(Long id);
    AmortizationTypeResponseDto create(AmortizationTypeCreateDto data);
    AmortizationTypeResponseDto edit(Long id, AmortizationTypeCreateDto data);
    AmortizationTypeResponseDto delete(Long id);
}
