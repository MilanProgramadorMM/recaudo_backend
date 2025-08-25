package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.AmortizationResponseDto;

import java.util.List;

public interface AmortizationGateway {

    List<AmortizationResponseDto> getAll();

}
