package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ClosingStatusGateway;
import com.recaudo.api.domain.gateway.CreditIntentionStatusGateway;
import com.recaudo.api.domain.model.dto.response.ClosingStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeClosingStatusDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@UseCase
public class ClosingStatusUseCase {

    private ClosingStatusGateway closingStatusGateway;


    public ClosingStatusResponseDto updateStatus(ChangeClosingStatusDto dto){
        return closingStatusGateway.updateStatus(dto);
    }

    public ClosingStatusResponseDto getCurrentStatus(Long closingId) {
        return closingStatusGateway.getCurrentStatus(closingId);
    }

    public List<ClosingStatusResponseDto> getStatusHistory(Long closingId) {
        return closingStatusGateway.getStatusHistory(closingId);
    }

}
