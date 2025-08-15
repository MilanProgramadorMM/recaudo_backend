package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.OptionDTO;
import com.recaudo.api.infrastructure.adapter.ParametrosUbicacionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ubicacion")
public class ParametrosUbicacionController {

    @Autowired
    ParametrosUbicacionAdapter parametrosUbicacionAdapter;

    @GetMapping("/paises")
    public List<OptionDTO> getPaises() {
        return parametrosUbicacionAdapter.listarPaises();
    }

    @GetMapping("/paises/{paisId}/departamentos")
    public List<OptionDTO> getDepartamentos(@PathVariable Long paisId) {
        return parametrosUbicacionAdapter.listarDepartamentosPorPais(paisId);
    }

    @GetMapping("/departamentos/{departamentoId}/municipios")
    public List<OptionDTO> getMunicipios(@PathVariable Long departamentoId) {
        return parametrosUbicacionAdapter.listarMunicipiosPorDepartamento(departamentoId);
    }

    @GetMapping("/municipios/{municipioId}/barrios")
    public List<OptionDTO> getBarrios(@PathVariable Long municipioId) {
        return parametrosUbicacionAdapter.listarBarriosPorMunicipio(municipioId);
    }
}
