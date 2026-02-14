// 3. Interfaz común para ambos gateways
package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.rest_api.DisbursementData;
import com.recaudo.api.domain.model.entity.BasePaymentEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CreditIntentionPaymentGateway<T extends BasePaymentEntity> {
    
    /**
     * Guarda múltiples pagos para una intención de crédito
     */
    List<T> savePayments(
            Long creditIntentionId,
            List<DisbursementData> disbursementDataList,
            List<MultipartFile> files
    ) throws Exception;

    /**
     * Encuentra pagos por ID de intención de crédito
     */
    List<T> findByCreditIntentionId(Long creditIntentionId);

    /**
     * Elimina un pago por ID (borrado lógico)
     */
    void deleteById(Long id);

    /**
     * Verifica si existe un pago por ID
     */
    boolean existsById(Long id);
}