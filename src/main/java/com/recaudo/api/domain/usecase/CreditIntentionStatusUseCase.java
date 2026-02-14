package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditIntentionGateway;
import com.recaudo.api.domain.gateway.CreditIntentionStatusGateway;
import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.IntentionCreditResponseAllDto;
import com.recaudo.api.domain.model.dto.response.ProyeccionAmortizacionDto;
import com.recaudo.api.domain.model.dto.rest_api.*;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@UseCase
public class CreditIntentionStatusUseCase {

    private CreditIntentionStatusGateway creditIntentionStatusGateway;


    public CreditIntentionStatusResponseDto updateStatus(ChangeCreditStatusDto dto){
        return creditIntentionStatusGateway.updateStatus(dto);
    }




}
