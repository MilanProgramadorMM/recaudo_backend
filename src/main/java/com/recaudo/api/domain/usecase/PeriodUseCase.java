package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.PeriodGateway;
import com.recaudo.api.domain.gateway.RolGateway;
import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.rest_api.RoleCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.UserRoleUpdateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class PeriodUseCase {

    private PeriodGateway periodGateway;


    public List<PeriodResponseDto> getAll() {
        return periodGateway.getAll();
    }

}
