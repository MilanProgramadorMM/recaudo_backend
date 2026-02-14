package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.IntentionCreditResponseAllDto;
import com.recaudo.api.domain.model.dto.rest_api.ApprovalDecisionRequest;
import com.recaudo.api.domain.model.dto.rest_api.PublicCreditIntentionResponse;
import com.recaudo.api.domain.model.dto.rest_api.SendApprovalLinkResponse;
import com.recaudo.api.domain.model.entity.CreditIntentionEntity;
import com.recaudo.api.infrastructure.helper.util.ApprovalStatus;
import com.recaudo.api.infrastructure.repository.CreditIntentionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CreditIntentionApprovalService {

    private static final Logger log = LoggerFactory.getLogger(CreditIntentionApprovalService.class);

    @Autowired
    private CreditIntentionRepository creditIntentionRepository;

    @Autowired
    private WhatsAppService whatsAppService;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    /**
     * Generar y enviar link de aprobación
     */
    public SendApprovalLinkResponse generateAndSendApprovalLink(Long intentionId, String whatsappNumber) {
        log.info("🔄 Generando link de aprobación para intención: {}", intentionId);

        CreditIntentionEntity intention = creditIntentionRepository.findById(intentionId)
                .orElseThrow(() -> new EntityNotFoundException("Intención de crédito no encontrada"));

        // Generar token único y seguro
        String token = generateSecureToken();
        log.debug("🔐 Token generado: {}", token);

        // Calcular fecha de expiración (7 días)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        // Construir link
        String approvalLink = String.format("%s/public/credit-approval/%s", frontendUrl, token);
        log.info("🔗 Link generado: {}", approvalLink);

        // Actualizar la intención
        intention.setApprovalToken(token);
        intention.setApprovalLink(approvalLink);
        intention.setTokenExpiresAt(expiresAt);
        intention.setApprovalStatus(ApprovalStatus.PENDING);

        creditIntentionRepository.save(intention);
        log.info("💾 Intención actualizada en BD");

        // Enviar WhatsApp
        String message = buildWhatsAppMessage(intention.getFullname(), approvalLink, expiresAt);
        whatsAppService.sendMessage(whatsappNumber, message);

        SendApprovalLinkResponse response = new SendApprovalLinkResponse();
        response.setApprovalLink(approvalLink);
        response.setMessage("Link de aprobación enviado exitosamente");
        response.setExpiresAt(String.valueOf(expiresAt));

        log.info("✅ Link de aprobación enviado exitosamente");
        return response;
    }
    /**
     * Obtener información pública de la intención por token
     */
    public PublicCreditIntentionResponse getPublicIntentionByToken(String token) {
        CreditIntentionEntity intention = creditIntentionRepository.findByApprovalToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Token inválido o expirado"));

        List<IntentionCreditResponseAllDto> dataIntention = creditIntentionRepository.findByIdProjection(intention.getId());

        if (dataIntention.isEmpty()) {
            throw new EntityNotFoundException("No se encontró información de la intención de crédito");
        }

        IntentionCreditResponseAllDto dto = dataIntention.get(0); // Tomamos el primer elemento

        // Verificar si el token expiró
        boolean isExpired = intention.getTokenExpiresAt() != null &&
                LocalDateTime.now().isAfter(intention.getTokenExpiresAt());

        // Mapear valores al response
        PublicCreditIntentionResponse response = new PublicCreditIntentionResponse();
        response.setId(dto.getId());
        response.setFullname(dto.getFullname());
        response.setNameLine(dto.getCreditLineName());
        response.setQuotaValue(dto.getQuotaValue());
        response.setPeriodQuantity(dto.getPeriodQuantity());
        response.setNamePeriod(dto.getPeriodName());
        response.setTotalIntentionValue(dto.getTotalIntentionValue());
        // Conversión de String a ApprovalStatus enum
        ApprovalStatus status;
        try {
            status = ApprovalStatus.valueOf(dto.getApprovalStatus().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            status = ApprovalStatus.PENDING;
        }
        response.setApprovalStatus(status);        response.setTokenExpired(isExpired);

        return response;
    }
    
    /**
     * Procesar decisión de aprobación
     */
    public void processApprovalDecision(ApprovalDecisionRequest request, String ipAddress) {
        CreditIntentionEntity intention = creditIntentionRepository.findByApprovalToken(request.getToken())
            .orElseThrow(() -> new EntityNotFoundException("Token inválido"));
        
        // Validar que el token no haya expirado
        if (intention.getTokenExpiresAt() != null && 
            LocalDateTime.now().isAfter(intention.getTokenExpiresAt())) {
            throw new IllegalStateException("El token ha expirado");
        }
        
        // Validar que no haya sido procesado previamente
        if (intention.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Esta solicitud ya fue procesada anteriormente");
        }
        
        // Actualizar estado
        intention.setApprovalStatus(request.isApproved() ? 
            ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        intention.setApprovedAt(LocalDateTime.now());
        intention.setApprovalIp(ipAddress);
        
        creditIntentionRepository.save(intention);
        
        // Aquí puedes agregar notificaciones adicionales, logs, etc.
    }
    
    /**
     * Reenviar link de aprobación
     */
    public SendApprovalLinkResponse resendApprovalLink(Long intentionId) {
        CreditIntentionEntity intention = creditIntentionRepository.findById(intentionId)
            .orElseThrow(() -> new EntityNotFoundException("Intención de crédito no encontrada"));
        
        // Si ya existe un link válido, reenviarlo
        if (intention.getApprovalToken() != null && 
            intention.getTokenExpiresAt() != null &&
            LocalDateTime.now().isBefore(intention.getTokenExpiresAt())) {
            
            sendWhatsAppMessage(intention.getWhatsappNumber(), 
                              intention.getFullname(), 
                              intention.getApprovalLink());
            
            SendApprovalLinkResponse response = new SendApprovalLinkResponse();
            response.setApprovalLink(intention.getApprovalLink());
            response.setMessage("Link reenviado exitosamente");
            response.setExpiresAt(String.valueOf(intention.getTokenExpiresAt()));
            
            return response;
        }
        
        // Si no hay link válido, generar uno nuevo
        return generateAndSendApprovalLink(intentionId, intention.getWhatsappNumber());
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    
    private void sendWhatsAppMessage(String whatsappNumber, String clientName, String link) {
        String message = String.format(
            "Hola %s,\n\n" +
            "Tu solicitud de crédito está lista para aprobación.\n\n" +
            "Por favor confirma tu solicitud ingresando al siguiente enlace:\n%s\n\n" +
            "Este enlace expirará en 7 días.\n\n" +
            "Si no solicitaste este crédito, ignora este mensaje.",
            clientName, link
        );
        
        // Integrar con tu servicio de WhatsApp (Twilio, WhatsApp Business API, etc.)
        // whatsAppService.sendMessage(whatsappNumber, message);
        
        System.out.println("WhatsApp enviado a " + whatsappNumber + ": " + message);
    }


    /**
     * Construir mensaje de WhatsApp
     */
    private String buildWhatsAppMessage(String clientName, String link, LocalDateTime expiresAt) {
        return String.format(
                "Hola %s,\n\n" +
                        "Tu solicitud de crédito está lista para aprobación.\n\n" +
                        "Por favor confirma tu solicitud ingresando al siguiente enlace:\n%s\n\n" +
                        "Este enlace expirará el %s.\n\n" +
                        "Si no solicitaste este crédito, ignora este mensaje.",
                clientName,
                link,
                expiresAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }
}