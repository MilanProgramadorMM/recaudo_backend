package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.CreditLineResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditLineCreateDto;
import com.recaudo.api.domain.usecase.CreditLineUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/credit-line")
@AllArgsConstructor
public class CreditLineController {

    private final CreditLineUseCase creditLineUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<CreditLineResponseDto>>> getAllCreditLine() {
        List<CreditLineResponseDto> persons = creditLineUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<CreditLineResponseDto>>builder()
                        .message("Lineas de credito encontradas")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(persons)
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<DefaultResponseDto<CreditLineResponseDto>> register(
            @RequestBody @Valid CreditLineCreateDto data, BindingResult
                    bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
                DefaultResponseDto.<CreditLineResponseDto>builder()
                        .message("Registro completado")
                        .status(HttpStatus.OK)
                        .details("Registro completado exitosamente")
                        .data(creditLineUseCase.register(data))
                        .build()
        );
    }

        @PutMapping("/update/{id}")
        public ResponseEntity<DefaultResponseDto<CreditLineResponseDto>> updatePerson(
                @PathVariable Long id,
                @RequestBody @Valid CreditLineCreateDto data,
                BindingResult bindingResult) throws Exception {

            if (bindingResult.hasErrors())
                throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

            return ResponseEntity.ok(
                    DefaultResponseDto.<CreditLineResponseDto>builder()
                            .message("Linea de credito actualizada correctamente")
                            .status(HttpStatus.OK)
                            .details("Los datos fueron modificados")
                            .data(creditLineUseCase.update(id,data))
                            .build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<DefaultResponseDto<Void>> delete(@PathVariable Long id) {
            creditLineUseCase.delete(id);
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
