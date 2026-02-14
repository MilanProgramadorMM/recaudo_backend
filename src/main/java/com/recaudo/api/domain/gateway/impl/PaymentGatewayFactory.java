// 9. Factory para seleccionar el gateway apropiado
package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.gateway.CreditIntentionPaymentGateway;
import com.recaudo.api.infrastructure.helper.util.CreditLineType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class PaymentGatewayFactory {

    private final CreditIntentionPaymentGateway disbursementGateway;
    private final CreditIntentionPaymentGateway initialPaymentGateway;

    public CreditIntentionPaymentGateway getGateway(CreditLineType creditLineType) {
        return switch (creditLineType) {
            case LIBRE_INVERSION -> disbursementGateway;
            case FINANCIAMIENTO -> initialPaymentGateway;
        };
    }
}