package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.ClosingSpendFileDto;
import com.recaudo.api.domain.model.dto.response.ClosingSpendResponseDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.usecase.ClosingSpendUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/closing-spend")
@AllArgsConstructor
public class ClosingSpendController {

    private final ClosingSpendUseCase closingSpendUseCase;

    @PostMapping("/spend")
    public ResponseEntity<?> registerSpend(
            @RequestParam Long closingId,
            @RequestParam Long spendTypeId,
            @RequestParam Long zonaId,
            @RequestParam Double amount,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam boolean isBase
    ) {
        try {
            ClosingSpendResponseDto  spend =
                    closingSpendUseCase.registerSpend(
                            closingId,
                            spendTypeId,
                            zonaId,
                            amount,
                            file,
                            description,
                            isBase
                    );

            return ResponseEntity.ok(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.OK)
                            .message("Registrado correctamente")
                            .data(spend)
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error al realizar registrar")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    @GetMapping("/closing/{closingId}")
    public ResponseEntity<DefaultResponseDto<List<ClosingSpendResponseDto>>> getSpendsByClosingId(
            @PathVariable Long closingId
    ) {

        return ResponseEntity.ok(
                DefaultResponseDto.<List<ClosingSpendResponseDto >>builder()
                        .status(HttpStatus.OK)
                        .message("Gastos del cierre")
                        .data(closingSpendUseCase.getSpendsByClosingId(closingId))
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @DeleteMapping("/{spendId}")
    public ResponseEntity<DefaultResponseDto<Void>> deactivateSpend(
            @PathVariable Long spendId
    ) {

        closingSpendUseCase.deactivateSpend(spendId);

        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .status(HttpStatus.OK)
                        .message("Gasto desactivado correctamente")
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @PutMapping("/{spendId}")
    public ResponseEntity<?> updateSpend(
            @PathVariable Long spendId,
            @RequestParam Long spendTypeId,
            @RequestParam Double amount,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String description
    ) {
        try {
            ClosingSpendResponseDto spend =
                    closingSpendUseCase.updateSpend(
                                spendId,
                            spendTypeId,
                            amount,
                            file,
                            description
                    );

            return ResponseEntity.ok(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.OK)
                            .message("Gasto actualizado correctamente")
                            .data(spend)
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error al actualizar gasto")
                            .details(e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    @GetMapping("/{spendId}/evidence")
    public ResponseEntity<byte[]> downloadEvidence(@PathVariable Long spendId) {

        ClosingSpendFileDto file = closingSpendUseCase.getFileSpend(spendId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (file.getContentType() != null) {
            mediaType = MediaType.parseMediaType(file.getContentType());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(mediaType)
                .body(file.getFileData());
    }
}