package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientListItemDto {
    private Long personId;
    private String clienteFullname;
    private String clienteDocumento;
    private Long totalCreditos;
    private java.math.BigDecimal saldoTotal;
    private Integer diasMoraMaximo;
}