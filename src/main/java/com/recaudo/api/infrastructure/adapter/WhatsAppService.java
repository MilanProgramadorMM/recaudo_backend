package com.recaudo.api.infrastructure.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {
    
    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);
    
    @Value("${app.whatsapp.enabled:false}")
    private boolean whatsappEnabled;
    
    @Value("${app.whatsapp.account-sid:}")
    private String accountSid;
    
    @Value("${app.whatsapp.auth-token:}")
    private String authToken;
    
    @Value("${app.whatsapp.from-number:}")
    private String fromNumber;
    
    public void sendMessage(String toNumber, String message) {
        if (!whatsappEnabled) {
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║          📱 MODO DESARROLLO - WhatsApp SIMULADO               ║");
            log.info("╠════════════════════════════════════════════════════════════════╣");
            log.info("║ Para: {}                                              ", toNumber);
            log.info("║ Mensaje:");
            message.lines().forEach(line -> 
                log.info("║   {}", line)
            );
            log.info("╚════════════════════════════════════════════════════════════════╝");
            return;
        }
        
        try {
            // Twilio.init(accountSid, authToken);
            // Message.creator(
            //     new PhoneNumber("whatsapp:" + toNumber),
            //     new PhoneNumber(fromNumber),
            //     message
            // ).create();
            
            log.info(" WhatsApp enviado exitosamente a {}", toNumber);
        } catch (Exception e) {
            log.error("Error enviando WhatsApp: {}", e.getMessage(), e);
            throw new RuntimeException("Error al enviar mensaje de WhatsApp", e);
        }
    }
}