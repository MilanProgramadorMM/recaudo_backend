package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.ClosingStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeClosingStatusDto;
import com.recaudo.api.domain.usecase.ClosingStatusUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/closing-status")
@AllArgsConstructor
public class ClosingStatusController {

    private final ClosingStatusUseCase closingStatusUsecase ;

    @PutMapping("/update")
    public ResponseEntity<DefaultResponseDto<ClosingStatusResponseDto>> changeStatus(
            @Valid @RequestBody ChangeClosingStatusDto dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
        }
        return ResponseEntity.ok(
                DefaultResponseDto.<ClosingStatusResponseDto>builder()
                        .message("Estado del cierre actualizado")
                        .status(HttpStatus.OK)
                        .details("El cierre cambió de fase correctamente")
                        .data(closingStatusUsecase.updateStatus(dto))
                        .build()
        );
    }

    @GetMapping("/current/{closingId}")
    public ResponseEntity<DefaultResponseDto<ClosingStatusResponseDto>> getCurrentStatus(
            @PathVariable Long closingId
    ) {
        return ResponseEntity.ok(
                DefaultResponseDto.<ClosingStatusResponseDto>builder()
                        .message("Estado actual del cierre")
                        .status(HttpStatus.OK)
                        .data(closingStatusUsecase.getCurrentStatus(closingId))
                        .build()
        );
    }

    @GetMapping("/history/{closingId}")
    public ResponseEntity<DefaultResponseDto<List<ClosingStatusResponseDto>>> getStatusHistory(
            @PathVariable Long closingId
    ) {
        return ResponseEntity.ok(
                DefaultResponseDto.<List<ClosingStatusResponseDto>>builder()
                        .message("Historial de estados del cierre")
                        .status(HttpStatus.OK)
                        .data(closingStatusUsecase.getStatusHistory(closingId))
                        .build()
        );
    }



}
