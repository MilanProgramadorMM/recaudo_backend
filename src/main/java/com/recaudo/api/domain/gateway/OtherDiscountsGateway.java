package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.OtherDiscountsResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.OtherDiscountsCreateDto;

import java.util.List;

public interface OtherDiscountsGateway {

    List<OtherDiscountsResponseDto> getAll();
    OtherDiscountsResponseDto getById(Long id);
    OtherDiscountsResponseDto create(OtherDiscountsCreateDto data);
    OtherDiscountsResponseDto edit(Long id, OtherDiscountsCreateDto data);
    OtherDiscountsResponseDto delete(Long id);

}
