package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditIntentionObservationGateway;
import com.recaudo.api.domain.model.dto.response.CreditIntentionObservationResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@UseCase
public class CreditIntentionObservationCase {

    private CreditIntentionObservationGateway creditIntentionObservationGateway;


    public CreditIntentionObservationResponseDto createIndividual(ChangeCreditStatusDto dto){
        return creditIntentionObservationGateway.createIndividual(dto);
    }

    public List<CreditIntentionObservationResponseDto> findByCreditIntentionId(Long creditIntentionId){
        return creditIntentionObservationGateway.findByCreditIntentionId(creditIntentionId);
    }

}
