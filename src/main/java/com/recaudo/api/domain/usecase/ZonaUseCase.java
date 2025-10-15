package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ZonaGateway;
import com.recaudo.api.domain.model.dto.response.ZonaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class ZonaUseCase {

    private ZonaGateway gateway;


    public List<ZonaResponseDto> getStatusTrue() {
        return gateway.getStatusTrue();
    }

    public List<ZonaResponseDto> getAll() {
        return gateway.getAll();
    }

    public ZonaResponseDto create(ZonaCreateDto zonaCreateDto) {
        return gateway.create(zonaCreateDto);
    }

    public ZonaResponseDto update(Long id, ZonaCreateDto zonaCreateDto) {
        return gateway.update(id, zonaCreateDto);
    }

    public void delete(Long id) {
        gateway.delete(id);
    }
}
