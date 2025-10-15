package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.InsuranceTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.InsuranceTypeCreateDto;
import com.recaudo.api.domain.usecase.InsuranceTypeUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/insurance-type")
@RequiredArgsConstructor
public class InsuranceTypeController {

    private final InsuranceTypeUseCase insureTypeUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<InsuranceTypeResponseDto>>> getAll() {
        List<InsuranceTypeResponseDto> data = insureTypeUseCase.getAll();
        return ResponseEntity.ok(
                DefaultResponseDto.<List<InsuranceTypeResponseDto>>builder()
                        .message("Tipos de seguro encontrados")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<InsuranceTypeResponseDto>> getById(@PathVariable Long id) {
        InsuranceTypeResponseDto data = insureTypeUseCase.getById(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<InsuranceTypeResponseDto>builder()
                        .message("Tipo de seguro encontrado")
                        .status(HttpStatus.OK)
                        .details("Consulta exitosa")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<InsuranceTypeResponseDto>> create(
            @RequestBody @Valid InsuranceTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        InsuranceTypeResponseDto data = insureTypeUseCase.create(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<InsuranceTypeResponseDto>builder()
                        .message("Tipo de seguro creado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron guardados")
                        .build()
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<InsuranceTypeResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody @Valid InsuranceTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        InsuranceTypeResponseDto data = insureTypeUseCase.edit(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<InsuranceTypeResponseDto>builder()
                        .message("Tipo de seguro actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<InsuranceTypeResponseDto>> delete(@PathVariable Long id) {
        InsuranceTypeResponseDto data = insureTypeUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<InsuranceTypeResponseDto>builder()
                        .message("Tipo de seguro eliminado correctamente")
                        .status(HttpStatus.OK)
                        .details("El registro fue inactivado")
                        .build()
        );
    }
}
