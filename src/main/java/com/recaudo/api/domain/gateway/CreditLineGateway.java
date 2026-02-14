package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.CreditLineResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditLineCreateDto;

import java.util.List;

public interface CreditLineGateway {

    List<CreditLineResponseDto> get();
    CreditLineResponseDto getById(Long id);
    CreditLineResponseDto create(CreditLineCreateDto barrioCreateDto);
    CreditLineResponseDto update(Long id, CreditLineCreateDto barrioCreateDto);
    CreditLineResponseDto delete(Long id);



}
