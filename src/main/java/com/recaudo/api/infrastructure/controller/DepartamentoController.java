package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.BarrioResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.DepartamentoResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.BarrioCreateDto;
import com.recaudo.api.domain.model.dto.rest_api.DepartamentoCreateDto;
import com.recaudo.api.infrastructure.adapter.DepartamentoAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamento")
@AllArgsConstructor
public class DepartamentoController {

    DepartamentoAdapter departamentoAdapter;

    @GetMapping("")
    public ResponseEntity<DefaultResponseDto<List<DepartamentoResponseDto>>> get(
            ) {

        List<DepartamentoResponseDto> data = departamentoAdapter.get();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<DepartamentoResponseDto>>builder()
                        .message("Información de departamentos")
                        .status(HttpStatus.OK)
                        .details("Se listó la información de departamentos")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("")
    public ResponseEntity<DefaultResponseDto<DepartamentoResponseDto>> create(
            @RequestBody DepartamentoCreateDto dto) {

        DepartamentoResponseDto result = departamentoAdapter.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DefaultResponseDto.<DepartamentoResponseDto>builder()
                        .message("Departamento creado")
                        .details("El departamento se creó correctamente")
                        .status(HttpStatus.CREATED)
                        .data(result)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<DepartamentoResponseDto>> update(
            @PathVariable Long id,
            @RequestBody DepartamentoCreateDto dto) {

        DepartamentoResponseDto result = departamentoAdapter.update(id, dto);
        return ResponseEntity.ok(
                DefaultResponseDto.<DepartamentoResponseDto>builder()
                        .message("Departamento actualizado")
                        .details("El departamento se actualizó correctamente")
                        .status(HttpStatus.OK)
                        .data(result)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<Void>> delete(@PathVariable Long id) {
        departamentoAdapter.delete(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .message("Departamento eliminado")
                        .details("El departamento fue marcado como inactivo")
                        .status(HttpStatus.OK)
                        .data(null)
                        .build()
        );
    }


}
