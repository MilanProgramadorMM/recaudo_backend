package com.davivienda.kata.domain.gateway;

import com.davivienda.kata.domain.model.dto.rest_api.LoginDto;

public interface AuthGateway {
    public String auth(LoginDto loginDto) throws Exception;
}
