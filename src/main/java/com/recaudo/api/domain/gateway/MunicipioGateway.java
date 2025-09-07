package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.MunicipioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.MunicipioCreateDto;

import java.util.List;

public interface MunicipioGateway {

    List<MunicipioResponseDto> get();
    MunicipioResponseDto create(MunicipioCreateDto municipioCreateDto);
    MunicipioResponseDto update(Long id,MunicipioCreateDto municipioCreateDto);
    void delete(Long id);


}
