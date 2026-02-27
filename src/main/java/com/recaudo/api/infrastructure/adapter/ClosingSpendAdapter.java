package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.ClosingSpendGateway;
import com.recaudo.api.domain.mapper.ClosingSpendMapper;
import com.recaudo.api.domain.model.dto.response.ClosingSpendFileDto;
import com.recaudo.api.domain.model.dto.response.ClosingSpendResponseDto;
import com.recaudo.api.domain.model.entity.ClosingSpendEntity;
import com.recaudo.api.domain.model.entity.GlotypesEntity;
import com.recaudo.api.exception.ResourceNotFoundException;
import com.recaudo.api.infrastructure.repository.ClosingSpendRepository;
import com.recaudo.api.infrastructure.repository.GlotypesRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ClosingSpendAdapter implements ClosingSpendGateway {

    private final ClosingSpendRepository closingSpendRepository;
    private final GlotypesRepository glotypesRepository;
    private final ClosingSpendMapper mapper;


    @Transactional
    @Override
    public ClosingSpendResponseDto saveSpend(
            Long closingId,
            Long spendTypeId,
            Long zonaId,
            Double amount,
            MultipartFile file,
            String description) throws IOException {

        boolean isBase = isBase(spendTypeId);

        if (isBase && amount == null && amount <= 0) {
            throw new IllegalArgumentException("Base debe ser mayor a cero");
        }


        // Evidencia obligatoria si NO es BASE
        if (!isBase && (file == null || file.isEmpty())) {
            throw new IllegalArgumentException(
                    "La evidencia es obligatoria para este tipo de gasto"
            );
        }

        // BASE solo una vez por cierre
        if (isBase &&
                closingSpendRepository
                        .existsByClosingIdAndSpendTypeIdAndStatusTrue(closingId, spendTypeId)) {

            throw new IllegalStateException("Ya existe una base registrada para este cierre");
        }

        ClosingSpendEntity spend = ClosingSpendEntity.builder()
                .closingId(closingId)
                .spendTypeId(spendTypeId)
                .amount(amount)
                .description(description)
                .fileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .fileSize(file != null ? file.getSize() : null)
                .fileData(file != null ? file.getBytes() : null)
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .status(true)
                .build();

        ClosingSpendEntity saved = closingSpendRepository.save(spend);


        return mapper.toDto(saved);
    }

    @Override
    public List<ClosingSpendResponseDto> getSpendsByClosingId(Long closingId) {
        List<ClosingSpendEntity> entities =
                closingSpendRepository.findByClosingIdAndStatusTrue(closingId);
        return mapper.toDtoList(entities);
    }

    @Override
    public ClosingSpendResponseDto getSpendsByClosingAndType(Long closingId, Long typeId) {
        ClosingSpendEntity ent = closingSpendRepository.findByClosingIdAndSpendTypeId(closingId, typeId);
        return mapper.toDto(ent);
    }

    @Transactional
    @Override
    public void deactivateSpend(Long spendId) {
        ClosingSpendEntity spend = closingSpendRepository.findById(spendId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Gasto no encontrado: " + spendId
                ));

        spend.setStatus(false);
        spend.setUserEdit(getUsernameToken());
        spend.setEditedAt(LocalDateTime.now());

        closingSpendRepository.save(spend);
    }

    @Override
    public ClosingSpendResponseDto updateSpend(
            Long spendId,
            Long spendTypeId,
            Double amount,
            MultipartFile file,
            String description
    ) throws IOException {

        // Buscar el gasto existente
        ClosingSpendEntity existingSpend = closingSpendRepository.findById(spendId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));

        // Determinar qué archivo usar
        byte[] fileData;
        String fileName;

        if (file != null && !file.isEmpty()) {
            // Usuario subió un nuevo archivo
            fileData = file.getBytes();
            fileName = file.getOriginalFilename();
        } else {
            // Mantener el archivo existente
            fileData = existingSpend.getFileData();
            fileName = existingSpend.getFileName();
        }

        // Actualizar los campos del gasto
        existingSpend.setSpendTypeId(spendTypeId);
        existingSpend.setAmount(amount);
        existingSpend.setDescription(description);
        existingSpend.setFileData(fileData);
        existingSpend.setFileName(fileName);
        existingSpend.setUserEdit(getUsernameToken());
        existingSpend.setEditedAt(LocalDateTime.now());

        // Guardar
        ClosingSpendEntity saved = closingSpendRepository.save(existingSpend);

        return mapper.toDto(saved);
    }

    @Override
    public ClosingSpendResponseDto getSpendById(Long spendId) {
        ClosingSpendEntity closingSpend = closingSpendRepository.findById(spendId).orElse(null);
        return mapper.toDto(closingSpend);
    }

    @Transactional
    @Override
    public ClosingSpendFileDto getFileBySpendId(Long spendId) {

        ClosingSpendEntity entity = closingSpendRepository.findById(spendId)
                .orElseThrow(() -> new ResourceNotFoundException("No encontrado"));

        if (entity.getFileData() == null) {
            throw new ResourceNotFoundException("No hay archivo asociado");
        }

        return new ClosingSpendFileDto(
                entity.getFileName(),
                entity.getContentType(),
                entity.getFileData()
        );
    }

    private boolean isBase(Long spendTypeId) {
        if (spendTypeId == null) {
            return false;
        }
        GlotypesEntity baseType = glotypesRepository.findByKeyAndCode("TIPGAS", "BASE").orElse(null);
        return baseType != null && baseType.getId().equals(spendTypeId);
    }


    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}
