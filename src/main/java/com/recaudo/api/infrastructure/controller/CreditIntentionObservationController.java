package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.CreditIntentionObservationResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import com.recaudo.api.domain.usecase.CreditIntentionObservationCase;
import com.recaudo.api.domain.usecase.CreditIntentionStatusUseCase;
import com.recaudo.api.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/credit-intention-observation")
@AllArgsConstructor
public class CreditIntentionObservationController {

    private final CreditIntentionObservationCase creditIntentionObservationCase ;

    @PostMapping("/save")
    public ResponseEntity<DefaultResponseDto<CreditIntentionObservationResponseDto>> changeStatus(
            @Valid @RequestBody ChangeCreditStatusDto dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
        }
        return ResponseEntity.ok(
                DefaultResponseDto.<CreditIntentionObservationResponseDto>builder()
                        .message("Observacion insertada")
                        .status(HttpStatus.OK)
                        .details("Observacion insertada correctamente")
                        .data(creditIntentionObservationCase.createIndividual(dto))
                        .build()
        );
    }

    @GetMapping("/by-credit/{creditIntentionId}")
    public ResponseEntity<DefaultResponseDto<List<CreditIntentionObservationResponseDto>>> getByCredit(
            @PathVariable Long creditIntentionId) {

        return ResponseEntity.ok(
                DefaultResponseDto.<List<CreditIntentionObservationResponseDto>>builder()
                        .message("Observaciones encontradas")
                        .status(HttpStatus.OK)
                        .details("Listado de observaciones de la intención de crédito")
                        .data(creditIntentionObservationCase.findByCreditIntentionId(creditIntentionId))
                        .build()
        );
    }


}
