package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import com.recaudo.api.domain.model.dto.response.RoleDto;

import java.util.List;

public interface PeriodGateway {

    List<PeriodResponseDto> getAll();

}
