// 7. Implementación del Gateway para Disbursement
package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.gateway.CreditIntentionPaymentGateway;
import com.recaudo.api.domain.model.dto.rest_api.DisbursementData;
import com.recaudo.api.domain.model.entity.CreditIntentionDisbursementEntity;
import com.recaudo.api.infrastructure.repository.CreditIntentionDisbursementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("disbursementGateway")
@AllArgsConstructor
public class CreditIntentionDisbursementGatewayImpl 
        implements CreditIntentionPaymentGateway<CreditIntentionDisbursementEntity> {

    private final CreditIntentionDisbursementRepository repository;

    @Override
    public List<CreditIntentionDisbursementEntity> savePayments(
            Long creditIntentionId,
            List<DisbursementData> disbursementDataList,
            List<MultipartFile> files
    ) throws Exception {
        
        List<CreditIntentionDisbursementEntity> savedEntities = new ArrayList<>();

        for (DisbursementData data : disbursementDataList) {
            CreditIntentionDisbursementEntity entity;

            if (data.getId() != null) {
                // Actualizar existente
                entity = repository.findById(data.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Desembolso no encontrado con ID: " + data.getId()));
                entity.setUpdatedAt(LocalDateTime.now());
            } else {
                // Crear nuevo
                entity = new CreditIntentionDisbursementEntity();
                entity.setCreditIntentionId(creditIntentionId);
                entity.setCreatedAt(LocalDateTime.now());
            }

            // Mapear datos comunes
            entity.setPaymentTypeId(data.getPaymentTypeId());
            entity.setBankId(data.getBankId());
            entity.setAccountNumber(data.getAccountNumber());
            entity.setAmount(data.getAmount());
            entity.setStatus(true);  // Activo

            // Manejar archivo si existe
            if (Boolean.TRUE.equals(data.getHasFile()) && data.getFileIndex() != null) {
                MultipartFile file = files.get(data.getFileIndex());
                entity.setFileName(file.getOriginalFilename());
                entity.setContentType(file.getContentType());
                entity.setFileSize(file.getSize());
                entity.setFileData(file.getBytes());
            }

            savedEntities.add(repository.save(entity));
        }

        return savedEntities;
    }

    @Override
    public List<CreditIntentionDisbursementEntity> findByCreditIntentionId(Long creditIntentionId) {
        return repository.findByCreditIntentionIdAndStatusTrue(creditIntentionId);
    }

    @Override
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus(false);
            entity.setUpdatedAt(LocalDateTime.now());
            repository.save(entity);
        });
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}