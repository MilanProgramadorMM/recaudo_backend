package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.CreditFullResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.*;

import java.util.List;

public interface CreditIGateway {

    List<CreditFullResponseDto> getAll();
    CreditResponseDto getById(Long id);
    CreditResponseDto getByPersonId(Long personId);
    CreditResponseDto create(CreditRegisterDto dto);
    //void delete(Long id);


}
