package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.AmortizationTypeResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.AmortizationTypeCreateDto;
import com.recaudo.api.domain.usecase.AmortizationTypeUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/amortization-type")
@AllArgsConstructor
public class AmortizationTypeController {

    private final AmortizationTypeUseCase amortizationUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<AmortizationTypeResponseDto>>> getAll() {
        List<AmortizationTypeResponseDto> data = amortizationUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<AmortizationTypeResponseDto>>builder()
                        .message("Tipos de amortización encontrados")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<AmortizationTypeResponseDto>> getById(@PathVariable Long id) {
        AmortizationTypeResponseDto data = amortizationUseCase.getById(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<AmortizationTypeResponseDto>builder()
                        .message("Tipo de amortización encontrado")
                        .status(HttpStatus.OK)
                        .details("Consulta exitosa")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<AmortizationTypeResponseDto>> create(
            @RequestBody @Valid AmortizationTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        AmortizationTypeResponseDto data = amortizationUseCase.create(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<AmortizationTypeResponseDto>builder()
                        .message("Tipo de amortización creado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron guardados")
                        .build()
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<AmortizationTypeResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody @Valid AmortizationTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        AmortizationTypeResponseDto data = amortizationUseCase.edit(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<AmortizationTypeResponseDto>builder()
                        .message("Tipo de amortización actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<AmortizationTypeResponseDto>> delete(@PathVariable Long id) {
        AmortizationTypeResponseDto data = amortizationUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<AmortizationTypeResponseDto>builder()
                        .message("Tipo de amortización eliminado correctamente")
                        .status(HttpStatus.OK)
                        .details("El registro fue inactivado")
                        .build()
        );
    }
}
