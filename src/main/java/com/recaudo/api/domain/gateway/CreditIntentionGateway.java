package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseDto;
import com.recaudo.api.domain.model.dto.response.IntentionCreditResponseAllDto;
import com.recaudo.api.domain.model.dto.response.ProyeccionAmortizacionDto;
import com.recaudo.api.domain.model.dto.response.SimulationResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CalculateCreditIntentionDto;
import com.recaudo.api.domain.model.dto.rest_api.ClientDataCreditIntentionUpdateDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditIntentionDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditIntentionUpdateDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateFechaTentativaCreditIntentionDto;

import java.util.List;

public interface CreditIntentionGateway {

    List<IntentionCreditResponseAllDto> getAll();
    List<IntentionCreditResponseAllDto> getAllIncludingClosed();
    List<IntentionCreditResponseAllDto> getAllIncludingClosedByUsername(String username);
    List<IntentionCreditResponseAllDto> getById(Long id);
    CreditIntentionResponseDto create(CreditIntentionDto creditIntentionDto, String token, Long personId);
    List<SimulationResponseDto> simulate(CalculateCreditIntentionDto creditIntentionDto);
     boolean existById(Long id);
    CreditIntentionResponseDto updateDataCreditIntention(Long id, CreditIntentionUpdateDto dto);
    CreditIntentionResponseDto updateDataClient(Long id, ClientDataCreditIntentionUpdateDto dto);
    CreditIntentionResponseDto updateFechaTentativaCreditIntention(Long id, UpdateFechaTentativaCreditIntentionDto dto);
    //void delete(Long id);


}
