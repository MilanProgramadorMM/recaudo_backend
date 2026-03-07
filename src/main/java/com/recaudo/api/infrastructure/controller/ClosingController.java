package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.ClosingResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.TodayClosingProjection;
import com.recaudo.api.domain.model.dto.rest_api.ApproveClosingDto;
import com.recaudo.api.domain.model.dto.rest_api.ClosingDto;
import com.recaudo.api.domain.usecase.ClosingUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/closing")
@AllArgsConstructor
public class ClosingController {

    private final ClosingUseCase closingUseCase;

    @GetMapping("/get/{id}")
    public ResponseEntity<DefaultResponseDto<ClosingResponseDto>> getById(@PathVariable Long id) {
            ClosingResponseDto response = closingUseCase.getById(id);

            return ResponseEntity.ok(
                    DefaultResponseDto.<ClosingResponseDto>builder()
                            .status(HttpStatus.OK)
                            .message("Cierre obtenido correctamente")
                            .data(response)
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );

    }

    @GetMapping("/{personId}/today")
    public ResponseEntity<DefaultResponseDto<TodayClosingProjection>> getTodayClosing(
            @PathVariable Long personId
    ) {
        return ResponseEntity.ok(
                DefaultResponseDto.<TodayClosingProjection>builder()
                        .status(HttpStatus.OK)
                        .message("Consulta de cierre del día")
                        .data(closingUseCase.getTodayClosingByPerson(personId))
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @GetMapping("/{personId}/{zonaId}/today")
    public ResponseEntity<DefaultResponseDto<TodayClosingProjection>> getTodayClosing(
            @PathVariable Long personId,@PathVariable Long zonaId
    ) {
        return ResponseEntity.ok(
                DefaultResponseDto.<TodayClosingProjection>builder()
                        .status(HttpStatus.OK)
                        .message("Consulta de cierre del día")
                        .data(closingUseCase.getTodayClosingByPersonAndZona(personId,zonaId))
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<DefaultResponseDto<List<ClosingResponseDto>>> getByPersonId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long personId
    ) {
        token = token.replace("Bearer ", "");
        List<ClosingResponseDto> dtos =  closingUseCase.getBypersonId(personId, token);
        return ResponseEntity.ok(
                DefaultResponseDto.<List<ClosingResponseDto>>builder()
                        .status(HttpStatus.OK)
                        .message("Consulta de cierre del día")
                        .data(dtos)
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @PostMapping("/save")
    public ResponseEntity<DefaultResponseDto<ClosingResponseDto>> save(@RequestBody ClosingDto dto) {
            ClosingResponseDto response = closingUseCase.save(dto);

            return ResponseEntity.ok(
                    DefaultResponseDto.<ClosingResponseDto>builder()
                            .status(HttpStatus.OK)
                            .message("Cierre creado correctamente")
                            .data(response)
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );


    }


    @PutMapping("/edit/{id}")
    public ResponseEntity<DefaultResponseDto<ClosingResponseDto>> edit(
            @PathVariable Long id,
            @RequestBody ClosingDto dto
    ) {
            ClosingResponseDto response = closingUseCase.edit(id, dto);

            return ResponseEntity.ok(
                    DefaultResponseDto.<ClosingResponseDto>builder()
                            .status(HttpStatus.OK)
                            .message("Cierre actualizado correctamente")
                            .data(response)
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );

        }

    @PostMapping("/approve")
    public ResponseEntity<DefaultResponseDto<ClosingResponseDto>> approveClosing(
            @RequestBody ApproveClosingDto dto
    ) {
        try {
            ClosingResponseDto closing = closingUseCase.approveClosing(dto);

            return ResponseEntity.ok(
                    DefaultResponseDto.<ClosingResponseDto>builder()
                            .status(HttpStatus.OK)
                            .message("Cierre aprobado exitosamente")
                            .data(closing)
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.<ClosingResponseDto>builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error al aprobar cierre")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }

}