package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.AgreementResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.PendingQuotaForAgreementDto;
import com.recaudo.api.domain.model.dto.rest_api.CreateAgreementRequestDto;
import com.recaudo.api.domain.usecase.DelayPenaltyAgreementUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delay-penalty-agreement")
@AllArgsConstructor
public class DelayPenaltyAgreementController {

    private final DelayPenaltyAgreementUseCase delayPenaltyAgreementUseCase;

    @GetMapping("/credit/{creditId}/pending-quotas")
    public ResponseEntity<DefaultResponseDto<List<PendingQuotaForAgreementDto>>>
    getPendingQuotas(@PathVariable Long creditId) {

        return ResponseEntity.ok(
                DefaultResponseDto.<List<PendingQuotaForAgreementDto>>builder()
                        .message("Cuotas pendientes consultadas")
                        .details("Cuotas vencidas con intereses moratorios")
                        .status(HttpStatus.OK)
                        .data(delayPenaltyAgreementUseCase.getPendingQuotasWithPenalties(creditId))
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<DefaultResponseDto<AgreementResponseDto>>
    createAgreement(@RequestBody CreateAgreementRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DefaultResponseDto.<AgreementResponseDto>builder()
                        .message("Pacto de pago creado")
                        .details("Pacto creado correctamente")
                        .status(HttpStatus.CREATED)
                        .data(delayPenaltyAgreementUseCase.createAgreement(request))
                        .build()
        );
    }

    @GetMapping("/credit/{creditId}")
    public ResponseEntity<DefaultResponseDto<List<AgreementResponseDto>>>
    getAgreementsByCreditId(@PathVariable Long creditId) {

        return ResponseEntity.ok(
                DefaultResponseDto.<List<AgreementResponseDto>>builder()
                        .message("Pactos consultados")
                        .details("Pactos asociados al crédito")
                        .status(HttpStatus.OK)
                        .data(delayPenaltyAgreementUseCase.getAgreementsByCreditId(creditId))
                        .build()
        );
    }

    @PatchMapping("/{agreementId}/status")
    public ResponseEntity<DefaultResponseDto<AgreementResponseDto>>
    updateStatus(@PathVariable Long agreementId, @RequestParam Boolean status) {

        return ResponseEntity.ok(
                DefaultResponseDto.<AgreementResponseDto>builder()
                        .message("Estado actualizado")
                        .details("Estado del pacto actualizado correctamente")
                        .status(HttpStatus.OK)
                        .data(delayPenaltyAgreementUseCase.updateAgreementStatus(agreementId, status))
                        .build()
        );
    }
}