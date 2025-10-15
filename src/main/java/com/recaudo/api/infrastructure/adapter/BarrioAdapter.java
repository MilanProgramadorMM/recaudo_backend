package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.BarrioGateway;
import com.recaudo.api.domain.model.dto.response.BarrioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.BarrioCreateDto;
import com.recaudo.api.domain.model.entity.BarrioEntity;
import com.recaudo.api.domain.model.entity.DepartamentoEntity;
import com.recaudo.api.domain.model.entity.MunicipioEntity;
import com.recaudo.api.domain.model.entity.PaisEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.BarrioRepository;
import com.recaudo.api.infrastructure.repository.DepartamentoRepository;
import com.recaudo.api.infrastructure.repository.MunicipioRepository;
import com.recaudo.api.infrastructure.repository.PaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarrioAdapter implements BarrioGateway {

    @Autowired
    DepartamentoRepository departamentoRepository;

    @Autowired
    PaisRepository paisRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    BarrioRepository barrioRepository;
    @Override
    public List<BarrioResponseDto> get() {
        return barrioRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(BarrioEntity::getStatus)
                .map(b -> {
                    // Buscar municipio
                    var municipioOpt = municipioRepository.findById(b.getIdMunicipio());
                    String nombreMunicipio = municipioOpt.map(MunicipioEntity::getValue).orElse(null);
                    Long idMunicipio = municipioOpt.map(MunicipioEntity::getId).orElse(null);

                    // Buscar departamento a partir del municipio
                    var departamentoOpt = municipioOpt
                            .flatMap(m -> departamentoRepository.findById(m.getIdDepartamento()));
                    String nombreDepartamento = departamentoOpt.map(DepartamentoEntity::getValue).orElse(null);
                    Long idDepartamento = departamentoOpt.map(DepartamentoEntity::getId).orElse(null);

                    // Buscar país a partir del departamento
                    var paisOpt = departamentoOpt
                            .flatMap(d -> paisRepository.findById(d.getIdPais()));
                    String nombrePais = paisOpt.map(PaisEntity::getValue).orElse(null);
                    Long idPais = paisOpt.map(PaisEntity::getId).orElse(null);

                    return BarrioResponseDto.builder()
                            .id(b.getId())
                            .nombre(b.getValue())
                            .description(b.getDescription())
                            .nombreMunicipio(nombreMunicipio)
                            .idMunicipio(idMunicipio)
                            .nombreDepartamento(nombreDepartamento)
                            .idDepartamento(idDepartamento)
                            .nombrePais(nombrePais)
                            .idPais(idPais)
                            .build();
                })
                .toList();
    }


    @Override
    public BarrioResponseDto create(BarrioCreateDto barrioCreateDto) {
        var muniOpt = municipioRepository.findById(barrioCreateDto.getIdMunicipio());
        if (muniOpt.isEmpty()) throw new BadRequestException("El municipio no existe");

        // Validar duplicado
        boolean exists = barrioRepository.existsByValueIgnoreCaseAndIdMunicipio(barrioCreateDto.getValue(), barrioCreateDto.getIdMunicipio());
        if (exists) {
            throw new BadRequestException("Ya existe un barrio con este nombre en el municipio");
        }

        BarrioEntity entity = BarrioEntity.builder()
                .value(barrioCreateDto.getValue().toUpperCase())
                .description(barrioCreateDto.getDescription().toUpperCase())
                .idMunicipio(barrioCreateDto.getIdMunicipio())
                .status(true)
                .build();

        barrioRepository.save(entity);

        var dptoOpt = departamentoRepository.findById(muniOpt.get().getIdDepartamento());
        String nombreDepartamento = dptoOpt.map(DepartamentoEntity::getValue).orElse(null);
        String nombrePais = dptoOpt
                .flatMap(d -> paisRepository.findById(d.getIdPais()))
                .map(PaisEntity::getValue)
                .orElse(null);

        return BarrioResponseDto.builder()
                .id(entity.getId())
                .nombre(entity.getValue())
                .nombreMunicipio(muniOpt.get().getValue())
                .nombreDepartamento(nombreDepartamento)
                .nombrePais(nombrePais)
                .build();
    }

    @Override
    public BarrioResponseDto update(Long id, BarrioCreateDto barrioCreateDto) {
        BarrioEntity barrio = barrioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El barrio no existe"));

        // Validar duplicado (ignorar el propio barrio)
        boolean exists = barrioRepository.existsByValueIgnoreCaseAndIdMunicipioAndIdNot(
                barrioCreateDto.getValue(), barrioCreateDto.getIdMunicipio(), id
        );
        if (exists) {
            throw new BadRequestException("Ya existe un barrio con este nombre en el municipio");
        }

        // Actualizar campos
        barrio.setValue(barrioCreateDto.getValue().toUpperCase());
        barrio.setDescription(barrioCreateDto.getDescription().toUpperCase());
        barrio.setIdMunicipio(barrioCreateDto.getIdMunicipio());

        barrioRepository.save(barrio);

        var municipioOpt = municipioRepository.findById(barrio.getIdMunicipio());
        String nombreMunicipio = municipioOpt.map(MunicipioEntity::getValue).orElse(null);
        Long idDepartamento = municipioOpt.map(MunicipioEntity::getIdDepartamento).orElse(null);

        var departamentoOpt = municipioOpt.flatMap(m -> departamentoRepository.findById(m.getIdDepartamento()));
        String nombreDepartamento = departamentoOpt.map(DepartamentoEntity::getValue).orElse(null);
        Long idPais = departamentoOpt.map(DepartamentoEntity::getIdPais).orElse(null);

        String nombrePais = departamentoOpt
                .flatMap(d -> paisRepository.findById(d.getIdPais()))
                .map(PaisEntity::getValue)
                .orElse(null);

        return BarrioResponseDto.builder()
                .id(barrio.getId())
                .nombre(barrio.getValue())
                .description(barrio.getDescription())
                .idMunicipio(barrio.getIdMunicipio())
                .nombreMunicipio(nombreMunicipio)
                .idDepartamento(idDepartamento)
                .nombreDepartamento(nombreDepartamento)
                .idPais(idPais)
                .nombrePais(nombrePais)
                .build();
    }

    @Override
    public void delete(Long id) {
        BarrioEntity barrio = barrioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El barrio no existe"));

        barrio.setStatus(false);
        barrioRepository.save(barrio);
    }

}
