package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.ClosingResponseDto;
import com.recaudo.api.domain.model.dto.response.TodayClosingProjection;
import com.recaudo.api.domain.model.dto.rest_api.ClosingDto;

import java.util.List;
import java.util.Optional;

public interface ClosingGateway {

    List<ClosingResponseDto> getByPersonId(Long personId);
    public ClosingResponseDto getById(Long id);
    public ClosingResponseDto save(ClosingDto dto);
    public ClosingResponseDto edit(Long id, ClosingDto dto);
    Optional<TodayClosingProjection> getTodayClosingByPerson(Long personId);
    Optional<TodayClosingProjection> getTodayClosingByPersonAndZona(Long personId, Long zonaID);
    ClosingResponseDto updateDeliveryAmounts(
            Long closingId,
            String deliveryType,
            Double amountAdmin,
            Double amountAsesor
    );
}
