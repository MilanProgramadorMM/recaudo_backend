package com.recaudo.api.domain.model.dto.rest_api;

import lombok.*;

import java.time.LocalDateTime;

// Response con el link generado
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendApprovalLinkResponse {
    private String approvalLink;
    private String message;
    private String expiresAt;
    // Getters y setters
}