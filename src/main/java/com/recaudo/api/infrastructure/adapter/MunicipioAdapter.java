package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.DepartamentoGateway;
import com.recaudo.api.domain.gateway.MunicipioGateway;
import com.recaudo.api.domain.model.dto.response.DepartamentoResponseDto;
import com.recaudo.api.domain.model.dto.response.MunicipioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.MunicipioCreateDto;
import com.recaudo.api.domain.model.entity.DepartamentoEntity;
import com.recaudo.api.domain.model.entity.MunicipioEntity;
import com.recaudo.api.domain.model.entity.PaisEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.DepartamentoRepository;
import com.recaudo.api.infrastructure.repository.MunicipioRepository;
import com.recaudo.api.infrastructure.repository.PaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MunicipioAdapter implements MunicipioGateway {

    @Autowired
    DepartamentoRepository departamentoRepository;

    @Autowired
    PaisRepository paisRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Override
    public List<MunicipioResponseDto> get() {
        return municipioRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(MunicipioEntity::getStatus)
                .map(m -> {
                    var departamentoOpt = departamentoRepository.findById(m.getIdDepartamento());

                    String nombreDepartamento = departamentoOpt.map(DepartamentoEntity::getValue).orElse(null);
                    Long idDepartamento = departamentoOpt.map(DepartamentoEntity::getId).orElse(null);

                    // Obtener país desde el departamento
                    var paisOpt = departamentoOpt.flatMap(d -> paisRepository.findById(d.getIdPais()));

                    String nombrePais = paisOpt.map(PaisEntity::getValue).orElse(null);
                    Long idPais = paisOpt.map(PaisEntity::getId).orElse(null);

                    return MunicipioResponseDto.builder()
                            .id(m.getId())
                            .nombre(m.getValue().toUpperCase())
                            .description(m.getDescription())
                            .nombreDepartamento(nombreDepartamento)
                            .idDepartamento(idDepartamento)
                            .nombrePais(nombrePais)
                            .idPais(idPais)
                            .build();
                })
                .toList();
    }


    @Override
    public MunicipioResponseDto create(MunicipioCreateDto municipioCreateDto) {
        // validar departamento
        var dptoOpt = departamentoRepository.findById(municipioCreateDto.getIdDepartamento());
        if (dptoOpt.isEmpty()) throw new BadRequestException("El departamento no existe");

        // Validar duplicado
        boolean exists = municipioRepository.existsByValueIgnoreCaseAndIdDepartamento(municipioCreateDto.getValue(), municipioCreateDto.getIdDepartamento());
        if (exists) {
            throw new BadRequestException("Ya existe un municipio con este nombre en el departamento");
        }

        MunicipioEntity entity = MunicipioEntity.builder()
                .value(municipioCreateDto.getValue().toUpperCase())
                .description(municipioCreateDto.getDescription().toUpperCase())
                .idDepartamento(municipioCreateDto.getIdDepartamento())
                .status(true)
                .build();

        municipioRepository.save(entity);

        String nombrePais = paisRepository.findById(dptoOpt.get().getIdPais())
                .map(p -> p.getValue()).orElse(null);

        return MunicipioResponseDto.builder()
                .id(entity.getId())
                .nombre(entity.getValue())
                .nombreDepartamento(dptoOpt.get().getValue())
                .nombrePais(nombrePais)
                .build();
    }


    @Override
    public MunicipioResponseDto update(Long id, MunicipioCreateDto dto) {
        MunicipioEntity municipio = municipioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El municipio no existe"));

        // Validar duplicado
        boolean exists = municipioRepository.existsByValueIgnoreCaseAndIdDepartamentoAndIdNot(
                dto.getValue(), dto.getIdDepartamento(), id
        );
        if (exists) {
            throw new BadRequestException("Ya existe un municipio con este nombre en el departamento");
        }

        // Actualizar campos
        municipio.setValue(dto.getValue().toUpperCase());
        municipio.setDescription(dto.getDescription().toUpperCase());
        municipio.setIdDepartamento(dto.getIdDepartamento());

        municipioRepository.save(municipio);

        var dptoOpt = departamentoRepository.findById(municipio.getIdDepartamento());
        String nombreDepartamento = dptoOpt.map(DepartamentoEntity::getValue).orElse(null);
        Long idPais = dptoOpt.map(DepartamentoEntity::getIdPais).orElse(null);
        String nombrePais = dptoOpt.flatMap(d -> paisRepository.findById(d.getIdPais()))
                .map(PaisEntity::getValue)
                .orElse(null);

        return MunicipioResponseDto.builder()
                .id(municipio.getId())
                .nombre(municipio.getValue())
                .description(municipio.getDescription())
                .idDepartamento(municipio.getIdDepartamento())
                .nombreDepartamento(nombreDepartamento)
                .idPais(idPais)
                .nombrePais(nombrePais)
                .build();
    }

    @Override
    public void delete(Long id) {
        MunicipioEntity municipio = municipioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El municipio no existe"));

        municipio.setStatus(false);
        municipioRepository.save(municipio);
    }

}
