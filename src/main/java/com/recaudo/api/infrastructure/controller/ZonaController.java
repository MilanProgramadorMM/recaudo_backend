package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.ZonaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ZonaCreateDto;
import com.recaudo.api.domain.usecase.ZonaUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zona")
@AllArgsConstructor
public class ZonaController {

    ZonaUseCase zonaUseCase;

    @GetMapping("/status")
    public ResponseEntity<DefaultResponseDto<List<ZonaResponseDto>>> getByStatus(
            ) {

        List<ZonaResponseDto> data = zonaUseCase.getStatusTrue();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<ZonaResponseDto>>builder()
                        .message("Información de zonas")
                        .status(HttpStatus.OK)
                        .details("Se listó la información de zonas")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<DefaultResponseDto<List<ZonaResponseDto>>> getAll(
    ) {

        List<ZonaResponseDto> data = zonaUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<ZonaResponseDto>>builder()
                        .message("Información de zonas")
                        .status(HttpStatus.OK)
                        .details("Se listó la información de zonas")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("")
    public ResponseEntity<DefaultResponseDto<ZonaResponseDto>> create(
            @RequestBody ZonaCreateDto dto) {

        ZonaResponseDto result = zonaUseCase.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DefaultResponseDto.<ZonaResponseDto>builder()
                        .message("Zona creado")
                        .details("El departamento se creó correctamente")
                        .status(HttpStatus.CREATED)
                        .data(result)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<ZonaResponseDto>> update(
            @PathVariable Long id,
            @RequestBody ZonaCreateDto dto) {

        ZonaResponseDto result = zonaUseCase.update(id, dto);
        return ResponseEntity.ok(
                DefaultResponseDto.<ZonaResponseDto>builder()
                        .message("Zona actualizado")
                        .details("El departamento se actualizó correctamente")
                        .status(HttpStatus.OK)
                        .data(result)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<Void>> delete(@PathVariable Long id) {
        zonaUseCase.delete(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .message("Zona eliminado")
                        .details("El departamento fue marcado como inactivo")
                        .status(HttpStatus.OK)
                        .data(null)
                        .build()
        );
    }


}
