package com.recaudo.api.domain.usecase;

import com.recaudo.api.domain.gateway.CreditIntentionGateway;
import com.recaudo.api.domain.gateway.CreditIntentionPaymentGateway;
import com.recaudo.api.domain.gateway.impl.PaymentGatewayFactory;
import com.recaudo.api.domain.model.dto.response.CreditIntentionPaymentResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DisbursementData;
import com.recaudo.api.domain.model.dto.rest_api.DisbursementMetadata;
import com.recaudo.api.domain.model.entity.BasePaymentEntity;
import com.recaudo.api.domain.model.constant.CreditLineType;
import com.recaudo.api.infrastructure.repository.GlotypesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CreditIntentionPaymentUseCase {

    private final PaymentGatewayFactory gatewayFactory;
    private final CreditIntentionGateway creditIntentionGateway;
    private final GlotypesRepository glotypesRepository;

    /**
     * Guarda múltiples pagos para una intención de crédito
     */
    public List<CreditIntentionPaymentResponseDto> savePayments(
            Long creditIntentionId,
            CreditLineType creditLineType,
            List<MultipartFile> files,
            List<DisbursementMetadata> metadata
    ) throws Exception {

        // Validar que existe la intención
        if (!creditIntentionGateway.existById(creditIntentionId)) {
            throw new IllegalArgumentException("La intención de crédito no existe");
        }

        // Validar metadata
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un pago");
        }

        // Validar archivos según metadata
        validateFiles(files, metadata);

        // Obtener el gateway apropiado según el tipo de línea
        CreditIntentionPaymentGateway gateway = gatewayFactory.getGateway(creditLineType);

        // Obtener IDs actuales en BD
        List<? extends BasePaymentEntity> existingPayments =
                gateway.findByCreditIntentionId(creditIntentionId);

        Set<Long> idsEnviados = metadata.stream()
                .map(DisbursementMetadata::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> idsExistentes = existingPayments.stream()
                .map(BasePaymentEntity::getId)
                .collect(Collectors.toSet());

        // Eliminar los que NO están en la lista enviada
        for (Long idExistente : idsExistentes) {
            if (!idsEnviados.contains(idExistente)) {
                gateway.deleteById(idExistente);
            }
        }

        // Convertir metadata a DisbursementData
        List<DisbursementData> disbursementDataList = convertMetadataToDisbursementData(metadata);

        // Guardar o actualizar en base de datos
        List<? extends BasePaymentEntity> savedEntities = gateway.savePayments(
                creditIntentionId,
                disbursementDataList,
                files != null ? files : new ArrayList<>()
        );

        return convertEntitiesToDtos(savedEntities);
    }

    /**
     * Obtiene pagos con nombres de tipo de pago y banco
     */
    public List<CreditIntentionPaymentResponseDto> getPaymentsWithNames(
            Long creditIntentionId,
            CreditLineType creditLineType
    ) {
        CreditIntentionPaymentGateway gateway = gatewayFactory.getGateway(creditLineType);
        List<? extends BasePaymentEntity> entities = gateway.findByCreditIntentionId(creditIntentionId);
        return convertEntitiesToDtos(entities);
    }

    /**
     * Elimina un pago específico (borrado lógico)
     */
    public void deleteById(Long id, CreditLineType creditLineType) {
        CreditIntentionPaymentGateway gateway = gatewayFactory.getGateway(creditLineType);
        gateway.deleteById(id);
    }

    /**
     * Convierte entidades a DTOs con nombres de glotypes
     */
    private List<CreditIntentionPaymentResponseDto> convertEntitiesToDtos(
            List<? extends BasePaymentEntity> entities) {
        return entities.stream()
                .map(this::convertEntityToDto)
                .toList();
    }

    /**
     * Convierte una entidad a DTO con nombres
     */
    private CreditIntentionPaymentResponseDto convertEntityToDto(BasePaymentEntity entity) {
        // Obtener nombre del tipo de pago
        String paymentTypeName = glotypesRepository.findById(entity.getPaymentTypeId())
                .map(glotype -> glotype.getName())
                .orElse(null);

        // Obtener nombre del banco si existe
        String bankName = entity.getBankId() != null
                ? glotypesRepository.findById(entity.getBankId())
                .map(glotype -> glotype.getName())
                .orElse(null)
                : null;

        return CreditIntentionPaymentResponseDto.builder()
                .id(entity.getId())
                .creditIntentionId(entity.getCreditIntentionId())
                .paymentTypeId(entity.getPaymentTypeId())
                .paymentTypeName(paymentTypeName)
                .bankId(entity.getBankId())
                .bankName(bankName)
                .accountNumber(entity.getAccountNumber())
                .amount(entity.getAmount())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .createdAt(entity.getCreatedAt() != null
                        ? entity.getCreatedAt().toString()
                        : null)
                .build();
    }

    /**
     * Convierte metadata a DisbursementData
     */
    private List<DisbursementData> convertMetadataToDisbursementData(List<DisbursementMetadata> metadata) {
        List<DisbursementData> result = new ArrayList<>();
        int fileIndex = 0;

        for (DisbursementMetadata meta : metadata) {
            DisbursementData data = DisbursementData.builder()
                    .id(meta.getId())
                    .paymentTypeId(meta.getPaymentTypeId())
                    .bankId(meta.getBankId())
                    .accountNumber(meta.getAccountNumber())
                    .amount(meta.getAmount())
                    .hasFile(meta.getHasFile())
                    .fileIndex(meta.getHasFile() ? fileIndex++ : null)
                    .build();

            result.add(data);
        }

        return result;
    }

    /**
     * Valida que los archivos coincidan con la metadata
     */
    private void validateFiles(List<MultipartFile> files, List<DisbursementMetadata> metadata) {
        long filesExpected = metadata.stream()
                .filter(m -> Boolean.TRUE.equals(m.getHasFile()))
                .count();

        long filesReceived = files != null ? files.size() : 0;

        if (filesExpected != filesReceived) {
            throw new IllegalArgumentException(
                    String.format("Se esperaban %d archivos pero se recibieron %d",
                            filesExpected, filesReceived)
            );
        }
    }
}