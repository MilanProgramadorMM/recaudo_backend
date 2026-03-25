package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.constant.CreditStatusCode;
import com.recaudo.api.domain.model.dto.response.CreditIntentionObservationResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;

import java.util.List;

public interface CreditIntentionObservationGateway {

    CreditIntentionObservationResponseDto create(Long creditIntentionId,
                                                 String observation,
                                                 String activity,
                                                 String statusStart,
                                                 String statusEnd );
    CreditIntentionObservationResponseDto createIndividual(ChangeCreditStatusDto dto);
    List<CreditIntentionObservationResponseDto> findByCreditIntentionId(Long creditIntentionId);


}
