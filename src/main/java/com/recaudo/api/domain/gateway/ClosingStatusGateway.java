package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.ClosingStatusResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeClosingStatusDto;
import com.recaudo.api.domain.model.constant.ClosingStatus;

import java.util.List;

public interface ClosingStatusGateway {

    ClosingStatusResponseDto updateStatus(ChangeClosingStatusDto dto);
    ClosingStatusResponseDto create(Long closingId, String userStart, ClosingStatus code);
    ClosingStatusResponseDto getCurrentStatus(Long closingId);

    List<ClosingStatusResponseDto> getStatusHistory(Long closingId);
}
