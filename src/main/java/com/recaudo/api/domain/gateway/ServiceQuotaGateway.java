package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.ServiceQuotaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ServiceQuotaCreateDto;

import java.util.List;

public interface ServiceQuotaGateway {

    List<ServiceQuotaResponseDto> getAll();
    ServiceQuotaResponseDto getById(Long id);
    ServiceQuotaResponseDto create(ServiceQuotaCreateDto data);
    ServiceQuotaResponseDto edit(Long id, ServiceQuotaCreateDto data);
    ServiceQuotaResponseDto delete(Long id);

}
