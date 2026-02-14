package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditIntentionDocumentGateway;
import com.recaudo.api.domain.gateway.CreditIntentionGateway;
import com.recaudo.api.domain.model.dto.response.CreditIntentionDocumentResponseDto;
import com.recaudo.api.domain.model.entity.CreditIntentionDocumentEntity;
import com.recaudo.api.infrastructure.helper.util.DocumentMetadata;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@AllArgsConstructor
@UseCase
public class CreditIntentionDocumentUseCaseJ {

    private CreditIntentionDocumentGateway creditIntentionDocumentGateway;
    private final CreditIntentionGateway creditIntentionGateway;



    public List<CreditIntentionDocumentEntity> saveDocument(
            Long intentionId,
            List<MultipartFile> files,
            List<DocumentMetadata> metadata
    ) throws IOException {

        if (!creditIntentionGateway.existById(intentionId)) {
            throw new IllegalArgumentException("La intención de crédito no existe");
        }

        // Validar archivos
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un documento");
        }

        // Validar metadata
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Metadata requerida para los documentos");
        }

        // Regla clave
        if (files.size() != metadata.size()) {
            throw new IllegalArgumentException(
                    "La cantidad de archivos no coincide con la metadata"
            );
        }

        //Validaciones de negocio (opcional pero recomendado)
        validateBusinessRules(files, metadata);

        return creditIntentionDocumentGateway
                .saveDocuments(intentionId, files, metadata);
    }

    public List<CreditIntentionDocumentEntity> getDocumentByIntentionId(Long id){
        return creditIntentionDocumentGateway.getDocumentsByIntentionId(id);
    }

    public Map<String, Object> getCedulaByIntentionId(Long intentionId) {

        if (!creditIntentionGateway.existById(intentionId)) {
            throw new IllegalArgumentException(
                    "La intención de crédito no existe con ID: " + intentionId
            );
        }

        CreditIntentionDocumentEntity cedula =
                creditIntentionDocumentGateway.getCedulaByIntentionId(intentionId);

        if (cedula == null) {
            throw new IllegalArgumentException(
                    "No se encontró documento de cédula para la intención: " + intentionId
            );
        }

        String base64Data = cedula.getFileData() != null
                ? Base64.getEncoder().encodeToString(cedula.getFileData())
                : null;

        Map<String, Object> response = new HashMap<>();
        response.put("id", cedula.getId());
        response.put("fileName", cedula.getFileName());
        response.put("contentType", cedula.getContentType());
        response.put("fileSize", cedula.getFileSize());
        response.put("fileDataBase64", base64Data);
        response.put("documentSide", cedula.getDocumentSide());
        response.put("createdAt", cedula.getCreatedAt().toString()); // ✅ Convertir a String
        response.put("intentionId", cedula.getCreditIntentionId());

        return response;
    }
    private void validateBusinessRules(
            List<MultipartFile> files,
            List<DocumentMetadata> metadata
    ) {

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException(
                        "El archivo supera el tamaño máximo permitido (5MB)"
                );
            }

            if (!file.getContentType().startsWith("image/")
                    && !file.getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException(
                        "Tipo de archivo no permitido: " + file.getContentType()
                );
            }
        }
    }

    public List<CreditIntentionDocumentResponseDto> getDocumentByIntentionIdBase64(Long id) {
        List<CreditIntentionDocumentEntity> documents = creditIntentionDocumentGateway.getDocumentsByIntentionId(id);

        List<CreditIntentionDocumentResponseDto> response = new ArrayList<>();
        for (CreditIntentionDocumentEntity doc : documents) {
            String base64Data = doc.getFileData() != null ? Base64.getEncoder().encodeToString(doc.getFileData()) : null;

            response.add(new CreditIntentionDocumentResponseDto(
                    doc.getId(),
                    doc.getDocumentationTypeId(),
                    doc.getDocumentSide(),
                    doc.getFileName(),
                    doc.getContentType(),
                    doc.getFileSize(),
                    base64Data
            ));
        }

        return response;
    }


}
