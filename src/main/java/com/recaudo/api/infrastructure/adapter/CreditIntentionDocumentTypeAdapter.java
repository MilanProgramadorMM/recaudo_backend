package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.CreditIntentionDocumentGateway;
import com.recaudo.api.domain.model.entity.CreditIntentionDocumentEntity;
import com.recaudo.api.infrastructure.helper.util.DocumentMetadata;
import com.recaudo.api.infrastructure.repository.CreditIntentionDocumentTypeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CreditIntentionDocumentTypeAdapter implements CreditIntentionDocumentGateway {

    private final CreditIntentionDocumentTypeRepository documentRepository;
    private final DocumentMergeService documentMergeService;

    @Transactional
    @Override
    public List<CreditIntentionDocumentEntity> saveDocuments(
            Long intentionId,
            List<MultipartFile> files,
            List<DocumentMetadata> metadata) throws IOException {

        List<CreditIntentionDocumentEntity> savedDocuments = new ArrayList<>();

        // Agrupar archivos por documentationTypeId
        Map<Long, List<DocumentMetadata>> groupedByType = metadata.stream()
                .collect(Collectors.groupingBy(DocumentMetadata::getDocumentationTypeId));

        for (Map.Entry<Long, List<DocumentMetadata>> entry : groupedByType.entrySet()) {
            Long documentationTypeId = entry.getKey();
            List<DocumentMetadata> metadataList = entry.getValue();

            // Si hay más de un archivo del mismo tipo, combinarlos
            if (metadataList.size() > 1) {
                savedDocuments.add(
                        saveMergedDocument(intentionId, files, metadataList, documentationTypeId)
                );
            } else {
                // Un solo archivo, guardarlo normalmente
                savedDocuments.add(
                        saveSingleDocument(intentionId, files, metadataList.get(0))
                );
            }
        }

        return savedDocuments;
    }

    @Override
    public CreditIntentionDocumentEntity getCedulaByIntentionId(Long intentionId) {
        // Buscar documento con documentSide = "COMPLETO" (cédula combinada)
        List<CreditIntentionDocumentEntity> documents = documentRepository
                .findByCreditIntentionIdAndDocumentSideAndStatusTrue(intentionId, "COMPLETO");

        if (!documents.isEmpty()) {
            return documents.get(0); // Retornar el documento combinado
        }

        // Si no hay documento combinado, buscar FRENTE o REVERSO
        // (por si aún están separados)
        documents = documentRepository
                .findByCreditIntentionIdAndStatusTrue(intentionId);

        return documents.stream()
                .filter(doc -> "FRENTE".equals(doc.getDocumentSide()) ||
                        "REVERSO".equals(doc.getDocumentSide()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Guarda múltiples archivos combinados en un solo PDF
     */
    private CreditIntentionDocumentEntity saveMergedDocument(
            Long intentionId,
            List<MultipartFile> allFiles,
            List<DocumentMetadata> metadataList,
            Long documentationTypeId) throws IOException {

        // Desactivar documentos viejos del mismo tipo
        deactivateExistingDocuments(intentionId, documentationTypeId);

        // Obtener los archivos a combinar
        List<MultipartFile> filesToMerge = metadataList.stream()
                .map(meta -> allFiles.get(meta.getFileIndex()))
                .collect(Collectors.toList());

        // Combinar en un solo PDF
        byte[] mergedPdfBytes = documentMergeService.mergeDocumentsToPdf(filesToMerge);

        // Crear nombre descriptivo
        String fileName = String.format(
                "cedula_completa_%s.pdf",
                LocalDateTime.now().toString().replace(":", "-")
        );

        // Guardar documento combinado
        CreditIntentionDocumentEntity document = new CreditIntentionDocumentEntity();
        document.setCreditIntentionId(intentionId);
        document.setDocumentationTypeId(documentationTypeId);
        document.setDocumentSide("COMPLETO"); // Indica que es frente + reverso
        document.setFileName(fileName);
        document.setContentType("application/pdf");
        document.setFileSize((long) mergedPdfBytes.length);
        document.setFileData(mergedPdfBytes);
        document.setUserCreate(getUsernameToken());
        document.setCreatedAt(LocalDateTime.now());
        document.setStatus(true);

        return documentRepository.save(document);
    }

    /**
     * Guarda un archivo individual
     */
    private CreditIntentionDocumentEntity saveSingleDocument(
            Long intentionId,
            List<MultipartFile> allFiles,
            DocumentMetadata meta) throws IOException {

        MultipartFile file = allFiles.get(meta.getFileIndex());

        // Desactivar documento viejo si existe
        deactivateExistingDocuments(
                intentionId,
                meta.getDocumentationTypeId(),
                meta.getDocumentSide()
        );

        CreditIntentionDocumentEntity document = new CreditIntentionDocumentEntity();
        document.setCreditIntentionId(intentionId);
        document.setDocumentationTypeId(meta.getDocumentationTypeId());
        document.setDocumentSide(meta.getDocumentSide());
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFileData(file.getBytes());
        document.setUserCreate(getUsernameToken());
        document.setCreatedAt(LocalDateTime.now());
        document.setStatus(true);

        return documentRepository.save(document);
    }

    /**
     * Desactiva documentos existentes por tipo y lado
     */
    private void deactivateExistingDocuments(
            Long intentionId,
            Long documentationTypeId,
            String documentSide) {

        documentRepository
                .findByCreditIntentionIdAndDocumentationTypeIdAndDocumentSideAndStatusTrue(
                        intentionId, documentationTypeId, documentSide
                )
                .ifPresent(existing -> {
                    existing.setStatus(false);
                    existing.setUserEdit(getUsernameToken());
                    existing.setEditedAt(LocalDateTime.now());
                    documentRepository.save(existing);
                });
    }

    /**
     * Desactiva todos los documentos de un tipo (para documentos combinados)
     */
    private void deactivateExistingDocuments(
            Long intentionId,
            Long documentationTypeId) {

        List<CreditIntentionDocumentEntity> existingDocs = documentRepository
                .findAllByCreditIntentionIdAndDocumentationTypeIdAndStatusTrue(
                        intentionId, documentationTypeId
                );

        existingDocs.forEach(doc -> {
            doc.setStatus(false);
            doc.setUserEdit(getUsernameToken());
            doc.setEditedAt(LocalDateTime.now());
            documentRepository.save(doc);
        });
    }

    @Override
    public List<CreditIntentionDocumentEntity> getDocumentsByIntentionId(Long intentionId) {
        return documentRepository.findByCreditIntentionIdAndStatusTrue(intentionId);
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}