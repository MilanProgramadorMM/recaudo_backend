package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.rest_api.LoginDto;

public interface AuthGateway {
    public String auth(LoginDto loginDto) throws Exception;
}
