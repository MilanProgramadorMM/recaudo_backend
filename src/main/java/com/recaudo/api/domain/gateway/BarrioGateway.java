package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.BarrioResponseDto;
import com.recaudo.api.domain.model.dto.response.DepartamentoResponseDto;
import com.recaudo.api.domain.model.dto.response.MunicipioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.BarrioCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.DepartamentoCreateDto;

import java.util.List;

public interface BarrioGateway {

    List<BarrioResponseDto> get();
    BarrioResponseDto create(BarrioCreateDto barrioCreateDto);
    BarrioResponseDto update(Long id, BarrioCreateDto barrioCreateDto);
    void delete(Long id);



}
