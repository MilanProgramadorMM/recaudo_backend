package com.recaudo.api.domain.model.dto.rest_api;

import lombok.*;

// Request para enviar link
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendApprovalLinkRequest {
    private Long intentionId;
    private String whatsappNumber;
    // Getters y setters
}