package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.ServiceQuotaResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ServiceQuotaCreateDto;
import com.recaudo.api.domain.usecase.ServiceQuotaUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/service-quota")
@RequiredArgsConstructor
public class ServiceQuotaController {

    private final ServiceQuotaUseCase serviceQuotaUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<ServiceQuotaResponseDto>>> getAll() {
        List<ServiceQuotaResponseDto> data = serviceQuotaUseCase.getAll();
        return ResponseEntity.ok(
                DefaultResponseDto.<List<ServiceQuotaResponseDto>>builder()
                        .message("Tipos de cargos por servicio encontrados")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<ServiceQuotaResponseDto>> getById(@PathVariable Long id) {
        ServiceQuotaResponseDto data = serviceQuotaUseCase.getById(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<ServiceQuotaResponseDto>builder()
                        .message("Tipo de cargos por servicio encontrado")
                        .status(HttpStatus.OK)
                        .details("Consulta exitosa")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<ServiceQuotaResponseDto>> create(
            @RequestBody @Valid ServiceQuotaCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ServiceQuotaResponseDto data = serviceQuotaUseCase.create(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<ServiceQuotaResponseDto>builder()
                        .message("Tipo de cargo por servicio creado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron guardados")
                        .build()
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<ServiceQuotaResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody @Valid ServiceQuotaCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ServiceQuotaResponseDto data = serviceQuotaUseCase.edit(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<ServiceQuotaResponseDto>builder()
                        .message("Tipo de cargo por servicio actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<ServiceQuotaResponseDto>> delete(@PathVariable Long id) {
        ServiceQuotaResponseDto data = serviceQuotaUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<ServiceQuotaResponseDto>builder()
                        .message("Tipo de cargo por servicio eliminado correctamente")
                        .status(HttpStatus.OK)
                        .details("El registro fue inactivado")
                        .build()
        );
    }
}
