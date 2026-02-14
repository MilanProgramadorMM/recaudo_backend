package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseProjection;
import com.recaudo.api.domain.model.dto.response.CreditRecaudoStatusDto;
import com.recaudo.api.domain.model.dto.response.RecaudoResponseProjection;
import com.recaudo.api.domain.model.dto.response.RecaudoResultDto;
import com.recaudo.api.domain.model.dto.rest_api.RecaudoRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.ReverseCapitalInterestRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.ReverseRecaudoRequestDto;
import com.recaudo.api.infrastructure.adapter.RecaudoAdapter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/recaudo")
public class RecaudoController {

    @Autowired
    private RecaudoAdapter recaudoAdapter;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processPayment(
            @RequestPart("data") @Valid RecaudoRequestDto request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {

            RecaudoResultDto result = recaudoAdapter.processPayment(
                    request,
                    file

            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("ERROR", e.getMessage()));
        }
    }


    /**
     * Endpoint para consultar el estado de pago de un crédito
     */
    @GetMapping("/status/{creditId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long creditId) {
        try {
            log.info("Consultando estado de pago para crédito ID: {}", creditId);

            CreditRecaudoStatusDto status = recaudoAdapter.getCreditPaymentStatus(creditId);

            log.info("Estado consultado exitosamente: {}% pagado, {} cuotas de {} pagadas",
                    status.getPorcentajePagado(),
                    status.getCuotasPagadas(),
                    status.getTotalCuotas());

            return ResponseEntity.ok(status);

        } catch (RuntimeException e) {
            log.error("Error de negocio al consultar estado del crédito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("BUSINESS_ERROR", e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al consultar estado del crédito", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Error al consultar estado"));
        }
    }

    @GetMapping("/user/{closinId}/date/{fecha}/zona/{zonaId}")
    public ResponseEntity<?> getRecaudosByUserAndDate(
            @PathVariable Long closinId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @PathVariable Long zonaId

    ) {
        try {
            log.info("Consultando recaudos del usuario {} para la fecha {}", closinId, fecha);

            List<RecaudoResponseProjection> recaudos =
                    recaudoAdapter.getRecaudosByUserAndDate(closinId, fecha, zonaId);

            return ResponseEntity.ok(recaudos);

        } catch (RuntimeException e) {
            log.error("Error de negocio al consultar recaudos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("BUSINESS_ERROR", e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al consultar recaudos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Error al consultar recaudos"));
        }
    }

    @GetMapping("/intentions/user/{closingId}/date/{fecha}/zona/{zonaId}")
    public ResponseEntity<?> getIntentionsByUserAndDate(
            @PathVariable Long closingId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @PathVariable Long zonaId
    ) {
        try {
            List<CreditIntentionResponseProjection> intentions =
                    recaudoAdapter.getIntentionsByUserAndDate(closingId, fecha, zonaId);

            return ResponseEntity.ok(intentions);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("BUSINESS_ERROR", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Error al consultar intenciones"));
        }
    }

    @PostMapping("/reverse/recaudos")
    public ResponseEntity<?> reverseRecaudos(@Valid @RequestBody ReverseRecaudoRequestDto requestDto) {
        try {
            RecaudoResultDto result = recaudoAdapter.reverseRecaudos(requestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al reversar recaudos: " + e.getMessage()));
        }
    }

    @PostMapping("/reverse/capital")
    public ResponseEntity<?> reverseCapital(@Valid @RequestBody ReverseCapitalInterestRequestDto requestDto) {
        try {
            RecaudoResultDto result = recaudoAdapter.reverseCapital(requestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al reversar capital: " + e.getMessage()));
        }
    }

    @PostMapping("/reverse/interest")
    public ResponseEntity<?> reverseInterest(@Valid @RequestBody ReverseCapitalInterestRequestDto requestDto) {
        try {
            RecaudoResultDto result = recaudoAdapter.reverseInterest(requestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al reversar intereses: " + e.getMessage()));
        }
    }

    /**
     * Método auxiliar para crear respuestas de error uniformes
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("errorCode", errorCode);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}