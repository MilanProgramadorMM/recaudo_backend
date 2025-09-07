package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.TaxTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.TaxTypeCreateDto;
import com.recaudo.api.domain.usecase.TaxTypeUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tax")
@RequiredArgsConstructor
public class TaxTypeController {

    private final TaxTypeUseCase taxTypeUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<TaxTypeResponseDto>>> getAll() {
        List<TaxTypeResponseDto> data = taxTypeUseCase.getAll();
        return ResponseEntity.ok(
                DefaultResponseDto.<List<TaxTypeResponseDto>>builder()
                        .message("Tipos de tasa encontrados")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<TaxTypeResponseDto>> getById(@PathVariable Long id) {
        TaxTypeResponseDto data = taxTypeUseCase.getById(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<TaxTypeResponseDto>builder()
                        .message("Tipo de tasa encontrado")
                        .status(HttpStatus.OK)
                        .details("Consulta exitosa")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<TaxTypeResponseDto>> create(
            @RequestBody @Valid TaxTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TaxTypeResponseDto data = taxTypeUseCase.create(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<TaxTypeResponseDto>builder()
                        .message("Tipo de tasa creado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron guardados")
                        .build()
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<TaxTypeResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody @Valid TaxTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TaxTypeResponseDto data = taxTypeUseCase.edit(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<TaxTypeResponseDto>builder()
                        .message("Tipo de tasa actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<TaxTypeResponseDto>> delete(@PathVariable Long id) {
        TaxTypeResponseDto data = taxTypeUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<TaxTypeResponseDto>builder()
                        .message("Tipo de tasa eliminado correctamente")
                        .status(HttpStatus.OK)
                        .details("El registro fue inactivado")
                        .build()
        );
    }
}
