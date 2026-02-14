package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.rest_api.ApprovalDecisionRequest;
import com.recaudo.api.domain.model.dto.rest_api.PublicCreditIntentionResponse;
import com.recaudo.api.infrastructure.adapter.CreditIntentionApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//Controlador público (sin autenticación)
@RestController
@RequestMapping("/public/credit-approval")
public class PublicCreditApprovalController {
    
    @Autowired
    private CreditIntentionApprovalService approvalService;
    
    /**
     * Obtener información de la intención por token (público)
     */
    @GetMapping("/{token}")
    public ResponseEntity<PublicCreditIntentionResponse> getIntentionByToken(
            @PathVariable String token) {
        PublicCreditIntentionResponse response = approvalService
            .getPublicIntentionByToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Procesar decisión de aprobación (público)
     */
    @PostMapping("/decision")
    public ResponseEntity<Map<String, String>> processDecision(
            @RequestBody ApprovalDecisionRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        approvalService.processApprovalDecision(request, ipAddress);

        return ResponseEntity.ok(
                Map.of("message", "Decisión procesada exitosamente")
        );
    }


    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}