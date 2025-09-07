package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ServiceQuotaGateway;
import com.recaudo.api.domain.model.dto.response.ServiceQuotaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ServiceQuotaCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class ServiceQuotaUseCase {

    private ServiceQuotaGateway serviceQuotaGateway;


    public List<ServiceQuotaResponseDto> getAll() {
        return serviceQuotaGateway.getAll();
    }

    public ServiceQuotaResponseDto getById(Long id) {
        return serviceQuotaGateway.getById(id);
    }

    public ServiceQuotaResponseDto create(ServiceQuotaCreateDto data) {
        return serviceQuotaGateway.create(data);
    }

    public ServiceQuotaResponseDto edit(Long id, ServiceQuotaCreateDto data) {
        return serviceQuotaGateway.edit(id, data);
    }

    public ServiceQuotaResponseDto delete(Long id) {
        return serviceQuotaGateway.delete(id);
    }

}
