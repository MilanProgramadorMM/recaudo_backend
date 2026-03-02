package com.recaudo.api.infrastructure.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recaudo.api.domain.model.dto.response.CreditIntentionPaymentResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DisbursementMetadata;
import com.recaudo.api.domain.usecase.CreditIntentionPaymentUseCase;
import com.recaudo.api.domain.model.constant.CreditLineType;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/credit-intention-disbursement")
@AllArgsConstructor
public class CreditIntentionDisbursementController {

    private final CreditIntentionPaymentUseCase paymentUseCase;

    /**
     * Endpoint para guardar pagos (desembolsos o pagos iniciales)
     */
    @PostMapping("/save")
    public ResponseEntity<?> savePayments(
            @RequestParam(value = "creditIntentionId") Long creditIntentionId,
            @RequestParam(value = "creditLineType") String creditLineTypeStr,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "metadata") String metadataJson
    ) {
        try {
            // Parsear tipo de línea de crédito
            CreditLineType creditLineType = CreditLineType.fromString(creditLineTypeStr);

            ObjectMapper mapper = new ObjectMapper();

            // Parsear metadata
            List<DisbursementMetadata> metadata = mapper.readValue(
                    metadataJson,
                    new TypeReference<List<DisbursementMetadata>>() {}
            );

            List<CreditIntentionPaymentResponseDto> responseDtos =
                    paymentUseCase.savePayments(
                            creditIntentionId,
                            creditLineType,
                            files,
                            metadata
                    );

            String entityName = creditLineType == CreditLineType.FINANCIAMIENTO
                    ? "pagos iniciales"
                    : "desembolsos";

            return ResponseEntity.ok(DefaultResponseDto.builder()
                    .status(HttpStatus.OK)
                    .message(entityName + " guardados correctamente")
                    .details("Se guardaron " + responseDtos.size() + " registro(s)")
                    .data(responseDtos)
                    .timestamp(LocalDateTime.now().toString())
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error de validación")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error al guardar pagos")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    /**
     * Endpoint para obtener pagos de una intención
     */
    @GetMapping("/{creditIntentionId}")
    public ResponseEntity<?> getPayments(
            @PathVariable Long creditIntentionId,
            @RequestParam(value = "creditLineType") String creditLineTypeStr
    ) {
        try {
            CreditLineType creditLineType = CreditLineType.fromString(creditLineTypeStr);

            List<CreditIntentionPaymentResponseDto> responseDtos =
                    paymentUseCase.getPaymentsWithNames(creditIntentionId, creditLineType);

            return ResponseEntity.ok(DefaultResponseDto.builder()
                    .status(HttpStatus.OK)
                    .message("Pagos obtenidos correctamente")
                    .data(responseDtos)
                    .timestamp(LocalDateTime.now().toString())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error al obtener pagos")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    /**
     * Endpoint para eliminar un pago (borrado lógico)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(
            @PathVariable Long id,
            @RequestParam(value = "creditLineType") String creditLineTypeStr
    ) {
        try {
            CreditLineType creditLineType = CreditLineType.fromString(creditLineTypeStr);
            paymentUseCase.deleteById(id, creditLineType);

            return ResponseEntity.ok(DefaultResponseDto.builder()
                    .status(HttpStatus.OK)
                    .message("Pago eliminado correctamente")
                    .timestamp(LocalDateTime.now().toString())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error al eliminar pago")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }
}