package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.OptionDTO;
import com.recaudo.api.infrastructure.repository.BarrioRepository;
import com.recaudo.api.infrastructure.repository.DepartamentoRepository;
import com.recaudo.api.infrastructure.repository.MunicipioRepository;
import com.recaudo.api.infrastructure.repository.PaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParametrosUbicacionAdapter {

    @Autowired
    private PaisRepository paisRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private MunicipioRepository municipioRepository;
    @Autowired
    private BarrioRepository barrioRepository;

    public ParametrosUbicacionAdapter(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<OptionDTO> listarPaises() {
        return paisRepository.findAllByStatusTrueOrderByValueAsc()
                .stream()
                .map(p -> new OptionDTO(p.getId(), p.getValue()))
                .toList();
    }

    public List<OptionDTO> listarDepartamentosPorPais(Long idPais) {
        return departamentoRepository.findByIdPaisAndStatusTrueOrderByValueAsc(idPais)
                .stream()
                .map(d -> new OptionDTO(d.getId(), d.getValue()))
                .toList();
    }

    public List<OptionDTO> listarMunicipiosPorDepartamento(Long idDepartamento) {
        return municipioRepository.findByIdDepartamentoAndStatusTrueOrderByValueAsc(idDepartamento)
                .stream()
                .map(m -> new OptionDTO(m.getId(), m.getValue()))
                .toList();
    }

    public List<OptionDTO> listarBarriosPorMunicipio(Long idMunicipio) {
        return barrioRepository.findByIdMunicipioAndStatusTrueOrderByValueAsc(idMunicipio)
                .stream()
                .map(b -> new OptionDTO(b.getId(), b.getValue()))
                .toList();
    }
}
