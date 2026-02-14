package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditIntentionGateway;
import com.recaudo.api.domain.gateway.impl.CreditIntentionDisbursementGatewayImpl;
import com.recaudo.api.domain.model.dto.response.CreditIntentionDisbursementResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.DisbursementData;
import com.recaudo.api.domain.model.dto.rest_api.DisbursementMetadata;
import com.recaudo.api.domain.model.entity.CreditIntentionDisbursementEntity;
import com.recaudo.api.domain.model.entity.CreditIntentionInitialPaymentEntity;
import com.recaudo.api.domain.model.entity.GlotypesEntity;
import com.recaudo.api.infrastructure.helper.util.CreditLineType;
import com.recaudo.api.infrastructure.repository.GlotypesRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UseCase
@AllArgsConstructor
public class CreditIntentionDisbursementUseCase {

    private final CreditIntentionDisbursementGatewayImpl disbursementGateway;
    private final CreditIntentionGateway creditIntentionGateway;
    private final CreditIntentionDisbursementGatewayImpl initialPaymentGateway;
    private final GlotypesRepository glotypesGateway;

    public List<CreditIntentionDisbursementResponseDto> saveDisbursements(
            Long creditIntentionId,
            String lineTypeStr,
            List<MultipartFile> files,
            List<DisbursementMetadata> metadata
    ) throws Exception {

        CreditLineType lineType = CreditLineType.fromString(lineTypeStr);

        // Validar que existe la intención
        if (!creditIntentionGateway.existById(creditIntentionId)) {
            throw new IllegalArgumentException("La intención de crédito no existe");
        }

        // Validar metadata
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un desembolso");
        }

        // Validar archivos según metadata
        validateFiles(files, metadata);

        // Seleccionar el gateway según el tipo
        List<?> existingDisbursements;
        Set<Long> idsExistentes;

        if (lineType == CreditLineType.FINANCIAMIENTO) {
            existingDisbursements = initialPaymentGateway.findByCreditIntentionId(creditIntentionId);
            idsExistentes = existingDisbursements.stream()
                    .map(e -> ((CreditIntentionInitialPaymentEntity) e).getId())
                    .collect(Collectors.toSet());
        } else {
            existingDisbursements = disbursementGateway.findByCreditIntentionId(creditIntentionId);
            idsExistentes = existingDisbursements.stream()
                    .map(e -> ((CreditIntentionDisbursementEntity) e).getId())
                    .collect(Collectors.toSet());
        }

        Set<Long> idsEnviados = metadata.stream()
                .map(DisbursementMetadata::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Eliminar los que NO están en la lista enviada
        for (Long idExistente : idsExistentes) {
            if (!idsEnviados.contains(idExistente)) {
                if (lineType == CreditLineType.FINANCIAMIENTO) {
                    initialPaymentGateway.deleteById(idExistente);
                } else {
                    disbursementGateway.deleteById(idExistente);
                }
            }
        }

        // Convertir metadata a DisbursementData
        List<DisbursementData> disbursementDataList = convertMetadataToDisbursementData(metadata);

        // Guardar según el tipo
        List<?> savedEntities;
        if (lineType == CreditLineType.FINANCIAMIENTO) {
            savedEntities = initialPaymentGateway.savePayments(
                    creditIntentionId,
                    disbursementDataList,
                    files != null ? files : new ArrayList<>()
            );
        } else {
            savedEntities = disbursementGateway.savePayments(
                    creditIntentionId,
                    disbursementDataList,
                    files != null ? files : new ArrayList<>()
            );
        }

        return convertEntitiesToDtos(savedEntities, lineType);
    }

    public List<CreditIntentionDisbursementResponseDto> getDisbursementsWithNames(
            Long creditIntentionId,
            String lineTypeStr
    ) {
        CreditLineType lineType = CreditLineType.fromString(lineTypeStr);

        List<?> entities;
        if (lineType == CreditLineType.FINANCIAMIENTO) {
            entities = initialPaymentGateway.findByCreditIntentionId(creditIntentionId);
        } else {
            entities = disbursementGateway.findByCreditIntentionId(creditIntentionId);
        }

        return convertEntitiesToDtos(entities, lineType);
    }

    private List<CreditIntentionDisbursementResponseDto> convertEntitiesToDtos(
            List<?> entities,
            CreditLineType lineType
    ) {
        return entities.stream()
                .map(entity -> convertEntityToDto(entity, lineType))
                .toList();
    }
    private CreditIntentionDisbursementResponseDto convertEntityToDto(
            Object entity,
            CreditLineType lineType
    ) {
        Long id, creditIntentionId, paymentTypeId, bankId;
        String accountNumber, fileName, contentType;
        Long fileSize;
        Double amount;
        Object createdAt;

        // Extraer campos según el tipo de entidad
        if (lineType == CreditLineType.FINANCIAMIENTO) {
            CreditIntentionInitialPaymentEntity e = (CreditIntentionInitialPaymentEntity) entity;
            id = e.getId();
            creditIntentionId = e.getCreditIntentionId();
            paymentTypeId = e.getPaymentTypeId();
            bankId = e.getBankId();
            accountNumber = e.getAccountNumber();
            amount = e.getAmount();
            fileName = e.getFileName();
            contentType = e.getContentType();
            fileSize = e.getFileSize();
            createdAt = e.getCreatedAt();
        } else {
            CreditIntentionDisbursementEntity e = (CreditIntentionDisbursementEntity) entity;
            id = e.getId();
            creditIntentionId = e.getCreditIntentionId();
            paymentTypeId = e.getPaymentTypeId();
            bankId = e.getBankId();
            accountNumber = e.getAccountNumber();
            amount = e.getAmount();
            fileName = e.getFileName();
            contentType = e.getContentType();
            fileSize = e.getFileSize();
            createdAt = e.getCreatedAt();
        }

        // Obtener nombres
        String paymentTypeName = glotypesGateway.findById(paymentTypeId)
                .map(GlotypesEntity::getName)
                .orElse(null);

        String bankName = bankId != null
                ? glotypesGateway.findById(bankId)
                .map(GlotypesEntity::getName)
                .orElse(null)
                : null;

        return CreditIntentionDisbursementResponseDto.builder()
                .id(id)
                .creditIntentionId(creditIntentionId)
                .paymentTypeId(paymentTypeId)
                .paymentTypeName(paymentTypeName)
                .bankId(bankId)
                .bankName(bankName)
                .accountNumber(accountNumber)
                .amount(amount)
                .fileName(fileName)
                .contentType(contentType)
                .fileSize(fileSize)
                .createdAt(createdAt != null ? createdAt.toString() : null)
                .build();
    }

    public void deleteById(Long id, String lineTypeStr) {
        CreditLineType lineType = CreditLineType.fromString(lineTypeStr);

        if (lineType == CreditLineType.FINANCIAMIENTO) {
            initialPaymentGateway.deleteById(id);
        } else {
            disbursementGateway.deleteById(id);
        }
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

    /**
     * Elimina todos los desembolsos de una intención (borrado lógico)
     */
    public void deleteAllByIntentionId(Long creditIntentionId) {
        List<CreditIntentionDisbursementEntity> existingDisbursements =
                disbursementGateway.findByCreditIntentionId(creditIntentionId);

        for (CreditIntentionDisbursementEntity entity : existingDisbursements) {
            disbursementGateway.deleteById(entity.getId());
        }
    }

    /*
     * @deprecated Usar getDisbursementsWithNames() en su lugar

    @Deprecated
    public List<CreditIntentionDisbursementEntity> getDisbursementsByIntentionId(Long creditIntentionId) {
        return disbursementGateway.findByCreditIntentionId(creditIntentionId);
    }
     */
}
