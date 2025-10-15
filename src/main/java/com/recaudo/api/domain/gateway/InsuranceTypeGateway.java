package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.InsuranceTypeResponseDto;
import com.recaudo.api.domain.model.dto.response.ServiceQuotaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.InsuranceTypeCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.ServiceQuotaCreateDto;

import java.util.List;

public interface InsuranceTypeGateway {

    List<InsuranceTypeResponseDto> getAll();
    InsuranceTypeResponseDto getById(Long id);
    InsuranceTypeResponseDto create(InsuranceTypeCreateDto data);
    InsuranceTypeResponseDto edit(Long id, InsuranceTypeCreateDto data);
    InsuranceTypeResponseDto delete(Long id);

}
