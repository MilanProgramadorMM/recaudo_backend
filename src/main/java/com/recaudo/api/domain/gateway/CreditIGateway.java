package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.CreditCausadoProjection;
import com.recaudo.api.domain.model.dto.response.CreditFullResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditProjection;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.*;

import java.util.List;

public interface CreditIGateway {

    List<CreditFullResponseDto> getAll();
    List<CreditFullResponseDto> getByUsername(String username);
    CreditResponseDto getById(Long id);
    List<CreditProjection> getByPersonId(Long personId);
    CreditResponseDto create(CreditRegisterDto dto);
    List<CreditFullResponseDto> getByAsesorUsername(String username);
    List<CreditCausadoProjection> getCreditsCausadosByClosing(Long closingId);
    //void delete(Long id);

}
