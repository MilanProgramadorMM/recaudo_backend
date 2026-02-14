package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditIGateway;
import com.recaudo.api.domain.gateway.DelayPenaltyAgreementGateway;
import com.recaudo.api.domain.model.dto.response.AgreementResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.response.PendingQuotaForAgreementDto;
import com.recaudo.api.domain.model.dto.rest_api.CreateAgreementRequestDto;
import com.recaudo.api.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@UseCase
public class DelayPenaltyAgreementUseCase {

    private final DelayPenaltyAgreementGateway delayPenaltyAgreementGateway;
    private final CreditIGateway creditIGateway;

    public List<PendingQuotaForAgreementDto> getPendingQuotasWithPenalties(Long creditId) {
        CreditResponseDto credit = creditIGateway.getById(creditId);
        if (credit == null) {
            throw new ResourceNotFoundException("Crédito no encontrado");
        }
        return delayPenaltyAgreementGateway.getPendingQuotasWithPenalties(creditId);
    }

    public AgreementResponseDto createAgreement(CreateAgreementRequestDto request) {
        CreditResponseDto credit = creditIGateway.getById(request.getCreditId());
        if (credit == null) {
            throw new ResourceNotFoundException("Crédito no encontrado");
        }
        return delayPenaltyAgreementGateway.createAgreement(request);
    }

    public List<AgreementResponseDto> getAgreementsByCreditId(Long creditId) {
        CreditResponseDto credit = creditIGateway.getById(creditId);
        if (credit == null) {
            throw new ResourceNotFoundException("Crédito no encontrado");
        }
        return delayPenaltyAgreementGateway.getAgreementsByCreditId(creditId);
    }

    public AgreementResponseDto updateAgreementStatus(Long agreementId, Boolean newStatus) {
        return delayPenaltyAgreementGateway.updateAgreementStatus(agreementId, newStatus);
    }
}