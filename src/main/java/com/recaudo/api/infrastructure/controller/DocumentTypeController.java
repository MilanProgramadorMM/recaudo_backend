package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.DocumentTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DocumentTypeCreateDto;
import com.recaudo.api.domain.usecase.DocumentTypeUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/document-type")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeUseCase documentTypeUseCase;

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<DocumentTypeResponseDto>>> getAll() {
        List<DocumentTypeResponseDto> data = documentTypeUseCase.getAll();
        return ResponseEntity.ok(
                DefaultResponseDto.<List<DocumentTypeResponseDto>>builder()
                        .message("Tipos de documento encontrados")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponseDto<DocumentTypeResponseDto>> getById(@PathVariable Long id) {
        DocumentTypeResponseDto data = documentTypeUseCase.getById(id);
        return ResponseEntity.ok(
                DefaultResponseDto.<DocumentTypeResponseDto>builder()
                        .message("Tipo de documento encontrado")
                        .status(HttpStatus.OK)
                        .details("Consulta exitosa")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DefaultResponseDto<DocumentTypeResponseDto>> create(
            @RequestBody @Valid DocumentTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        DocumentTypeResponseDto data = documentTypeUseCase.create(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<DocumentTypeResponseDto>builder()
                        .message("Tipo de documento creado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron guardados")
                        .build()
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<DocumentTypeResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody @Valid DocumentTypeCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        DocumentTypeResponseDto data = documentTypeUseCase.edit(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<DocumentTypeResponseDto>builder()
                        .message("Tipo de documento actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<DocumentTypeResponseDto>> delete(@PathVariable Long id) {
        DocumentTypeResponseDto data = documentTypeUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<DocumentTypeResponseDto>builder()
                        .message("Tipo de documento eliminado correctamente")
                        .status(HttpStatus.OK)
                        .details("El registro fue inactivado")
                        .build()
        );
    }
}
