package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.BarrioGateway;
import com.recaudo.api.domain.model.dto.response.BarrioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.BarrioCreateDto;
import com.recaudo.api.domain.model.entity.BarrioEntity;
import com.recaudo.api.domain.model.entity.DepartamentoEntity;
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
import java.util.Optional;

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
                    var municipioOpt = municipioRepository.findById(b.getIdMunicipio());
                    String nombreMunicipio = municipioOpt.map(m -> m.getValue()).orElse(null);
                    Long idMunicipio = municipioOpt.map(m -> m.getId()).orElse(null);

                    var departamentoOpt = municipioOpt
                            .flatMap(m -> departamentoRepository.findById(m.getIdDepartamento()));
                    String nombreDepartamento = departamentoOpt.map(d -> d.getValue()).orElse(null);

                    String nombrePais = departamentoOpt
                            .flatMap(d -> paisRepository.findById(d.getIdPais()))
                            .map(p -> p.getValue())
                            .orElse(null);

                    return BarrioResponseDto.builder()
                            .id(b.getId())
                            .nombre(b.getValue())
                            .description(b.getDescription())
                            .nombreMunicipio(nombreMunicipio)
                            .idMunicipio(idMunicipio)
                            .nombreDepartamento(nombreDepartamento)
                            .nombrePais(nombrePais)
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
        // Buscar el barrio
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

        // Obtener datos de ubicación
        var muniOpt = municipioRepository.findById(barrio.getIdMunicipio());
        String nombreMunicipio = muniOpt.map(m -> m.getValue()).orElse(null);
        var departamentoOpt = muniOpt.flatMap(m -> departamentoRepository.findById(m.getIdDepartamento()));
        String nombreDepartamento = departamentoOpt.map(DepartamentoEntity::getValue).orElse(null);
        String nombrePais = departamentoOpt
                .flatMap(d -> paisRepository.findById(d.getIdPais()))
                .map(PaisEntity::getValue)
                .orElse(null);

        return BarrioResponseDto.builder()
                .id(barrio.getId())
                .nombre(barrio.getValue())
                .nombreMunicipio(nombreMunicipio)
                .nombreDepartamento(nombreDepartamento)
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
