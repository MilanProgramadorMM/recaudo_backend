package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.*;
import com.recaudo.api.domain.usecase.CreditIntentionStatusUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/credit-intention-status")
@AllArgsConstructor
public class CreditIntentionStatusController {

    private final CreditIntentionStatusUseCase creditIntentionStatusUseCase ;

    @PutMapping("/update-credit")
    public ResponseEntity<DefaultResponseDto<CreditIntentionStatusResponseDto>> changeStatus(
            @Valid @RequestBody ChangeCreditStatusDto dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
        }
        return ResponseEntity.ok(
                DefaultResponseDto.<CreditIntentionStatusResponseDto>builder()
                        .message("Estado de la intención de crédito actualizado")
                        .status(HttpStatus.OK)
                        .details("La intención de crédito cambió de fase correctamente")
                        .data(creditIntentionStatusUseCase.updateStatus(dto))
                        .build()
        );
    }


}
