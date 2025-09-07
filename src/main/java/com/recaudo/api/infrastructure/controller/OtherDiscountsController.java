package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.OtherDiscountsResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.OtherDiscountsCreateDto;
import com.recaudo.api.domain.usecase.OtherDiscountsUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
public class OtherDiscountsController {

    private final OtherDiscountsUseCase otherDiscountsUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<OtherDiscountsResponseDto>>> getAll() {
        List<OtherDiscountsResponseDto> data = otherDiscountsUseCase.getAll();
        return ResponseEntity.ok(
                DefaultResponseDto.<List<OtherDiscountsResponseDto>>builder()
                        .message("Tipos de descuento encontrados")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<OtherDiscountsResponseDto>> getById(@PathVariable Long id) {
        OtherDiscountsResponseDto data = otherDiscountsUseCase.getById(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<OtherDiscountsResponseDto>builder()
                        .message("Tipo de descuento encontrado")
                        .status(HttpStatus.OK)
                        .details("Consulta exitosa")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<OtherDiscountsResponseDto>> create(
            @RequestBody @Valid OtherDiscountsCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        OtherDiscountsResponseDto data = otherDiscountsUseCase.create(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<OtherDiscountsResponseDto>builder()
                        .message("Tipo de descuento creado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron guardados")
                        .build()
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<OtherDiscountsResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody @Valid OtherDiscountsCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        OtherDiscountsResponseDto data = otherDiscountsUseCase.edit(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<OtherDiscountsResponseDto>builder()
                        .message("Tipo de descuento actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<OtherDiscountsResponseDto>> delete(@PathVariable Long id) {
        OtherDiscountsResponseDto data = otherDiscountsUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<OtherDiscountsResponseDto>builder()
                        .message("Tipo de descuento eliminado correctamente")
                        .status(HttpStatus.OK)
                        .details("El registro fue inactivado")
                        .build()
        );
    }
}
