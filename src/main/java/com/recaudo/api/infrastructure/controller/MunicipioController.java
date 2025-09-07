package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.DepartamentoResponseDto;
import com.recaudo.api.domain.model.dto.response.MunicipioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DepartamentoCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.MunicipioCreateDto;
import com.recaudo.api.infrastructure.adapter.MunicipioAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/municipios")
@AllArgsConstructor
public class MunicipioController {

    MunicipioAdapter municipioAdapter;

    @GetMapping("")
    public ResponseEntity<DefaultResponseDto<List<MunicipioResponseDto>>> get(
            ) {

        List<MunicipioResponseDto> data = municipioAdapter.get();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<MunicipioResponseDto>>builder()
                        .message("Información de municipios")
                        .status(HttpStatus.OK)
                        .details("Se listó la información de municipios")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("")
    public ResponseEntity<DefaultResponseDto<MunicipioResponseDto>> create(
            @RequestBody MunicipioCreateDto dto) {

        MunicipioResponseDto result = municipioAdapter.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DefaultResponseDto.<MunicipioResponseDto>builder()
                        .message("Municipio creado")
                        .details("El municipio se creó correctamente")
                        .status(HttpStatus.CREATED)
                        .data(result)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<MunicipioResponseDto>> update(
            @PathVariable Long id,
            @RequestBody MunicipioCreateDto dto) {

        MunicipioResponseDto result = municipioAdapter.update(id, dto);
        return ResponseEntity.ok(
                DefaultResponseDto.<MunicipioResponseDto>builder()
                        .message("Municipio actualizado")
                        .details("El municipio se actualizó correctamente")
                        .status(HttpStatus.OK)
                        .data(result)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<Void>> delete(@PathVariable Long id) {
        municipioAdapter.delete(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .message("Municipio eliminado")
                        .details("El municipio fue marcado como inactivo")
                        .status(HttpStatus.OK)
                        .data(null)
                        .build()
        );
    }


}
