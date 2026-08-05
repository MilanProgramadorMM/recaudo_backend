package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.rest_api.*;

import java.util.List;

public interface CreditIGateway {

    List<CreditFullResponseDto> getAll();
    List<CreditFullResponseDto> getByUsername(String username);
    CreditResponseDto getById(Long id);
    List<CreditProjection> getByPersonId(Long personId);
    CreditResponseDto create(CreditRegisterDto dto);
    List<CreditFullResponseDto> getByAsesorUsername(String username);
    List<CreditFullResponseDto> getByAsesorZone(Long personId);
    List<CreditCausadoProjection> getCreditsCausadosByClosing(Long closingId);
    CierreAsesorView findCierreAsesor(Long personId, Long creditId);
    //void delete(Long id);

}
