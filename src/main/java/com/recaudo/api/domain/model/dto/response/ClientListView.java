package com.recaudo.api.domain.model.dto.response;

import java.math.BigDecimal;

public interface ClientListView {
    Long getPersonId();
    String getClienteFullname();
    String getClienteDocumento();
    Long getTotalCreditos();
    BigDecimal getSaldoTotal();
    Integer getDiasMoraMaximo();
}