package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalDecisionRequest {
    private String token;
    private boolean approved;
    private String comments;
}