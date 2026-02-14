package com.recaudo.api.domain.gateway;


import com.recaudo.api.domain.model.dto.response.AgreementResponseDto;
import com.recaudo.api.domain.model.dto.response.PendingQuotaForAgreementDto;
import com.recaudo.api.domain.model.dto.rest_api.CreateAgreementRequestDto;

import java.util.List;

public interface DelayPenaltyAgreementGateway {

    List<PendingQuotaForAgreementDto> getPendingQuotasWithPenalties(Long creditId);

    AgreementResponseDto createAgreement(CreateAgreementRequestDto request);

    List<AgreementResponseDto> getAgreementsByCreditId(Long creditId);

    AgreementResponseDto updateAgreementStatus(Long agreementId, Boolean newStatus);
}