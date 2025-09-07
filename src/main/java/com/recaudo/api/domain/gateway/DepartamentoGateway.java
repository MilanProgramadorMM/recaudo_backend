package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.DepartamentoResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DepartamentoCreateDto;

import java.util.List;

public interface DepartamentoGateway {

    List<DepartamentoResponseDto> get();
    DepartamentoResponseDto create(DepartamentoCreateDto departamentoCreateDto);
    DepartamentoResponseDto update(Long id, DepartamentoCreateDto departamentoCreateDto);
    void delete(Long id);


}
