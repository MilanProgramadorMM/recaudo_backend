package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.*;
import com.recaudo.api.infrastructure.helper.util.CreditStatusCode;

public interface CreditIntentionStatusGateway {

    CreditIntentionStatusResponseDto updateStatus(ChangeCreditStatusDto dto);
    CreditIntentionStatusResponseDto create(Long creditId, String userStart, CreditStatusCode code);



}
