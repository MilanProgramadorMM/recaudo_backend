package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.BarrioResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.BarrioCreateDto;
import com.recaudo.api.infrastructure.adapter.BarrioAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barrios")
@AllArgsConstructor
public class BarrioController {

    BarrioAdapter barrioAdapter;

    @GetMapping("")
    public ResponseEntity<DefaultResponseDto<List<BarrioResponseDto>>> get(
            ) {

        List<BarrioResponseDto> data = barrioAdapter.get();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<BarrioResponseDto>>builder()
                        .message("Información de barrios")
                        .status(HttpStatus.OK)
                        .details("Se listó la información de barrios")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("")
    public ResponseEntity<DefaultResponseDto<BarrioResponseDto>> create(
            @RequestBody BarrioCreateDto dto) {

        BarrioResponseDto result = barrioAdapter.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DefaultResponseDto.<BarrioResponseDto>builder()
                        .message("Barrio creado")
                        .details("El barrio se creó correctamente")
                        .status(HttpStatus.CREATED)
                        .data(result)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<BarrioResponseDto>> update(
            @PathVariable Long id,
            @RequestBody BarrioCreateDto dto) {

        BarrioResponseDto result = barrioAdapter.update(id, dto);
        return ResponseEntity.ok(
                DefaultResponseDto.<BarrioResponseDto>builder()
                        .message("Barrio actualizado")
                        .details("El barrio se actualizó correctamente")
                        .status(HttpStatus.OK)
                        .data(result)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<Void>> delete(@PathVariable Long id) {
        barrioAdapter.delete(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .message("Barrio eliminado")
                        .details("El barrio fue marcado como inactivo")
                        .status(HttpStatus.OK)
                        .data(null)
                        .build()
        );
    }


}
