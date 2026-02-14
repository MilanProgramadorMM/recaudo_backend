package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.CreditFullResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditRegisterDto;
import com.recaudo.api.domain.model.entity.CreditEntity;
import com.recaudo.api.domain.usecase.CreditUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("credits")
public class CreditController {

    @Autowired
    private CreditUseCase creditUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<CreditFullResponseDto>>> getAll() {
        List<CreditFullResponseDto> data = creditUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<CreditFullResponseDto>>builder()
                        .message("Créditos obtenidos exitosamente")
                        .status(HttpStatus.OK)
                        .details("Lista de créditos obtenida correctamente")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<DefaultResponseDto<CreditResponseDto>> getById(@PathVariable Long id) {
        CreditResponseDto data = creditUseCase.getById(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<CreditResponseDto>builder()
                        .message("Crédito obtenido exitosamente")
                        .status(HttpStatus.OK)
                        .details("Crédito encontrado con ID: " + id)
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/get-by-person/{id}")
    public ResponseEntity<DefaultResponseDto<CreditResponseDto>> getByPersonId(
            @PathVariable Long id) {

        CreditResponseDto data = creditUseCase.getByPersonId(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<CreditResponseDto>builder()
                        .message("Crédito obtenido exitosamente")
                        .status(HttpStatus.OK)
                        .details("Crédito encontrado para la persona con ID: " + id)
                        .data(data)
                        .build()
        );
    }


    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<CreditResponseDto>> create(@RequestBody CreditRegisterDto dto) {
        CreditResponseDto data = creditUseCase.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DefaultResponseDto.<CreditResponseDto>builder()
                        .message("Crédito creado exitosamente")
                        .status(HttpStatus.CREATED)
                        .details("El crédito ha sido causado y registrado en cartera")
                        .data(data)
                        .build()
        );
    }
}