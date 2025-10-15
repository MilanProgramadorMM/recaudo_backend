package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.ZonaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;

import java.util.List;

public interface ZonaGateway {

    List<ZonaResponseDto> getStatusTrue();
    List<ZonaResponseDto> getAll();
    ZonaResponseDto create(ZonaCreateDto departamentoCreateDto);
    ZonaResponseDto update(Long id, ZonaCreateDto departamentoCreateDto);
    void delete(Long id);


}
