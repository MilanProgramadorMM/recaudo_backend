package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.DepartamentoGateway;
import com.recaudo.api.domain.model.dto.response.DepartamentoResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DepartamentoCreateDto;
import com.recaudo.api.domain.model.entity.DepartamentoEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.DepartamentoRepository;
import com.recaudo.api.infrastructure.repository.PaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoAdapter implements DepartamentoGateway {

    @Autowired
    DepartamentoRepository departamentoRepository;

    @Autowired
    PaisRepository paisRepository;

    @Override
    public List<DepartamentoResponseDto> get() {
        return departamentoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(DepartamentoEntity::getStatus)
                .map(d -> DepartamentoResponseDto.builder()
                        .id(d.getId())
                        .nombre(d.getValue())
                        .description(d.getDescription())
                        .nombrePais(
                                paisRepository.findById(d.getIdPais())
                                        .map(p -> p.getValue())
                                        .orElse(null)
                        )
                        .idPais(d.getIdPais())

                        .build()
                )
                .toList();
    }

    @Override
    public DepartamentoResponseDto create(DepartamentoCreateDto dto) {

        boolean exists = departamentoRepository.existsByValueIgnoreCaseAndIdPais(dto.getValue(), dto.getIdPais());
        if (exists) {
            throw new BadRequestException("Ya existe un departamento con este nombre para el país dado");
        }
        // validar país
        var paisOpt = paisRepository.findById(dto.getIdPais());
        if (paisOpt.isEmpty()) throw new BadRequestException("El país no existe");

        DepartamentoEntity entity = DepartamentoEntity.builder()
                .value(dto.getValue().toUpperCase())
                .description(dto.getDescription().toUpperCase())
                .idPais(dto.getIdPais())
                .status(true)
                .build();

        departamentoRepository.save(entity);

        return DepartamentoResponseDto.builder()
                .id(entity.getId())
                .nombre(entity.getValue())
                .nombrePais(paisOpt.get().getValue())
                .build();
    }

    @Override
    public DepartamentoResponseDto update(Long id, DepartamentoCreateDto dto) {
        DepartamentoEntity departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El departamento no existe"));

        // Validar duplicado
        boolean exists = departamentoRepository.existsByValueIgnoreCaseAndIdPaisAndIdNot(
                dto.getValue(), dto.getIdPais(), id
        );
        if (exists) {
            throw new BadRequestException("Ya existe un departamento con este nombre para el país dado");
        }

        // Actualizar campos
        departamento.setValue(dto.getValue().toUpperCase());
        departamento.setDescription(dto.getDescription().toUpperCase());
        departamento.setIdPais(dto.getIdPais());

        departamentoRepository.save(departamento);

        String nombrePais = paisRepository.findById(departamento.getIdPais())
                .map(p -> p.getValue())
                .orElse(null);

        return DepartamentoResponseDto.builder()
                .id(departamento.getId())
                .nombre(departamento.getValue())
                .nombrePais(nombrePais)
                .build();
    }

    @Override
    public void delete(Long id) {
        DepartamentoEntity departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El departamento no existe"));

        departamento.setStatus(false);
        departamentoRepository.save(departamento);
    }

}
