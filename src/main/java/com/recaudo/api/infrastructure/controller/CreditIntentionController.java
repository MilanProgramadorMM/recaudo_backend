package com.recaudo.api.infrastructure.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.rest_api.*;
import com.recaudo.api.domain.model.entity.CreditIntentionDocumentEntity;
import com.recaudo.api.domain.usecase.CreditIntentionDocumentUseCaseJ;
import com.recaudo.api.domain.usecase.CreditIntentionUseCase;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.adapter.CreditIntentionApprovalService;
import com.recaudo.api.infrastructure.helper.util.DocumentMetadata;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/credit-intention")
@AllArgsConstructor
public class CreditIntentionController {

    private final CreditIntentionUseCase creditIntentionUseCase;
    private  final CreditIntentionDocumentUseCaseJ creditIntentionDocumentUseCase;

    @Autowired
    private CreditIntentionApprovalService approvalService;

    @PostMapping("/generate/calculation")
    public ResponseEntity<DefaultResponseDto<List<SimulationResponseDto>>> simulate(
            @Valid @RequestBody CalculateCreditIntentionDto dto
    ) {

        List<SimulationResponseDto> result =
                creditIntentionUseCase.simulationIntention(dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<SimulationResponseDto>>builder()
                        .message("Proyeccion obtenida")
                        .status(HttpStatus.OK)
                        .details("Proyeccion de credito")
                        .data(result)
                        .build()
                );
    }

    @PutMapping("/update-client/{id}")
    public ResponseEntity<DefaultResponseDto<CreditIntentionResponseDto>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientDataCreditIntentionUpdateDto dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors())
            throw new BadRequestException(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );

        CreditIntentionResponseDto result =
                creditIntentionUseCase.updateDataClient(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<CreditIntentionResponseDto>builder()
                        .message("Datos del cliente actualizados correctamente")
                        .status(HttpStatus.OK)
                        .details("Actualización de datos del cliente en la intención de crédito")
                        .data(result)
                        .build()
        );
    }

    @PutMapping("/update-date/{id}")
    public ResponseEntity<DefaultResponseDto<CreditIntentionResponseDto>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFechaTentativaCreditIntentionDto dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors())
            throw new BadRequestException(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );

        CreditIntentionResponseDto result =
                creditIntentionUseCase.updateFechaTentativaClient(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<CreditIntentionResponseDto>builder()
                        .message("Datos de la fecha tentativa actualizada correctamente")
                        .status(HttpStatus.OK)
                        .details("Actualización de fecha tentativa en la intención de crédito")
                        .data(result)
                        .build()
        );
    }

    @PutMapping("/update-credit/{id}")
    public ResponseEntity<DefaultResponseDto<CreditIntentionResponseDto>> updateCredit(
            @PathVariable Long id,
            @Valid @RequestBody CreditIntentionUpdateDto dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors())
            throw new BadRequestException(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );

        CreditIntentionResponseDto result =
                creditIntentionUseCase.updateDataCreditIntention(id, dto);

        return ResponseEntity.ok(
                DefaultResponseDto.<CreditIntentionResponseDto>builder()
                        .message("Datos del crédito actualizados correctamente")
                        .status(HttpStatus.OK)
                        .details("Actualización de información financiera de la intención de crédito")
                        .data(result)
                        .build()
        );
    }


    @GetMapping("/get-intention/{id}")
    public ResponseEntity<DefaultResponseDto<List<IntentionCreditResponseAllDto>>> getById(
            @PathVariable("id") Long id){
        List<IntentionCreditResponseAllDto> data = creditIntentionUseCase.getById(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<IntentionCreditResponseAllDto>>builder()
                        .message("Intencion de credito obtenida")
                        .status(HttpStatus.OK)
                        .details("Intencion de credito obtenida")
                        .data(data)
                        .build()
        );

    }

    @GetMapping("/get-intention")
    public ResponseEntity<DefaultResponseDto<List<IntentionCreditResponseAllDto>>> getAll(){
        List<IntentionCreditResponseAllDto> data = creditIntentionUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<IntentionCreditResponseAllDto>>builder()
                        .message("Intenciones de credito obtenidas")
                        .status(HttpStatus.OK)
                        .details("Intenciones de credito obtenidas")
                        .data(data)
                        .build()
        );

    }

    @PostMapping("/create-with-documents")
    public ResponseEntity<?> createIntentionWithCedula(
            @RequestPart("intention") String intentionJson,
            @RequestPart(value = "documents", required = false) List<MultipartFile> files,
            @RequestPart(value = "metadata", required = false) String metadataJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            CreditIntentionDto intentionDTO = mapper.readValue(
                    intentionJson,
                    CreditIntentionDto.class
            );

            List<DocumentMetadata> metadata = metadataJson != null
                    ? mapper.readValue(metadataJson, new TypeReference<>() {})
                    : null;

            CreditIntentionResponseDto response =
                    creditIntentionUseCase.createWithDocuments(
                            intentionDTO,
                            files,
                            metadata
                    );

            return ResponseEntity.ok(DefaultResponseDto.builder()
                    .status(HttpStatus.OK)
                    .message("Intención de crédito creada exitosamente")
                    .data(response)
                    .timestamp(LocalDateTime.now().toString())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error al crear la intención")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }


    @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadDocuments(
            @RequestParam(value = "creditIntentionId") Long creditIntentionId,
            @RequestPart(value = "documents") List<MultipartFile> files,
            @RequestPart(value = "metadata", required = false) String metadataJson
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            List<DocumentMetadata> metadata = mapper.readValue(
                    metadataJson,
                    new TypeReference<List<DocumentMetadata>>() {}
            );

            // Guardar documentos (insert/update)
            List<CreditIntentionDocumentEntity> savedDocsEntities =
                    creditIntentionDocumentUseCase.saveDocument(
                            creditIntentionId,
                            files,
                            metadata
                    );

            // Convertir a DTO con Base64
            List<CreditIntentionDocumentResponseDto> savedDocsDto = savedDocsEntities.stream()
                    .map(doc -> new CreditIntentionDocumentResponseDto(
                            doc.getId(),
                            doc.getDocumentationTypeId(),
                            doc.getDocumentSide(),
                            doc.getFileName(),
                            doc.getContentType(),
                            doc.getFileSize(),
                            doc.getFileData() != null ? Base64.getEncoder().encodeToString(doc.getFileData()) : null
                    ))
                    .toList();

            return ResponseEntity.ok(DefaultResponseDto.builder()
                    .status(HttpStatus.OK)
                    .message("Documentos guardados correctamente")
                    .details("Se cargaron " + savedDocsDto.size() + " documento(s)")
                    .data(savedDocsDto)
                    .timestamp(LocalDateTime.now().toString())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error al guardar documentos")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }


    @GetMapping(value = "/{intentionId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CreditIntentionDocumentResponseDto>> getDocuments(@PathVariable Long intentionId) {
        List<CreditIntentionDocumentResponseDto> documents = creditIntentionDocumentUseCase.getDocumentByIntentionIdBase64(intentionId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{intentionId}/cedula")
    public ResponseEntity<?> getCedulaDocument(@PathVariable Long intentionId) {
        try {
            Map<String, Object> cedulaData =
                    creditIntentionDocumentUseCase.getCedulaByIntentionId(intentionId);

            return ResponseEntity.ok(DefaultResponseDto.builder()
                    .status(HttpStatus.OK)
                    .message("Cédula encontrada")
                    .data(cedulaData)
                    .timestamp(LocalDateTime.now().toString())
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DefaultResponseDto.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(DefaultResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error al obtener cédula")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        }
    }

    /**
     * Enviar link de aprobación (endpoint protegido)
     */
    @PostMapping("/{id}/send-approval-link")
    public ResponseEntity<SendApprovalLinkResponse> sendApprovalLink(
            @PathVariable Long id,
            @RequestBody SendApprovalLinkRequest request) {
        SendApprovalLinkResponse response = approvalService
                .generateAndSendApprovalLink(id, request.getWhatsappNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Reenviar link de aprobación (endpoint protegido)
     */
    @PostMapping("/{id}/resend-approval-link")
    public ResponseEntity<SendApprovalLinkResponse> resendApprovalLink(@PathVariable Long id) {
        SendApprovalLinkResponse response = approvalService.resendApprovalLink(id);
        return ResponseEntity.ok(response);
    }

}
