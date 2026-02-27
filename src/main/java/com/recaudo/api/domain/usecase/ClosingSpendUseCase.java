package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ClosingGateway;
import com.recaudo.api.domain.gateway.ClosingSpendGateway;
import com.recaudo.api.domain.model.dto.response.ClosingSpendFileDto;
import com.recaudo.api.domain.model.dto.response.ClosingSpendResponseDto;
import com.recaudo.api.domain.model.entity.ClosingSpendEntity;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@UseCase
public class ClosingSpendUseCase {

    private final ClosingSpendGateway closingSpendGateway;
    private final ClosingGateway closingGateway;

    public ClosingSpendResponseDto registerSpend(
            Long closingId,
            Long spendTypeId,
            Long zonaId,
            Double amount,
            MultipartFile file,
            String description,
            boolean isBase
    ) throws IOException {

        // Validar que el cierre exista
        if (closingGateway.getById(closingId) == null) {
            throw new IllegalArgumentException("El cierre no existe");
        }

        // Regla de negocio: BASE no requiere evidencia
        if (isBase) {
            return closingSpendGateway.saveSpend(
                    closingId,
                    spendTypeId,
                    zonaId,
                    amount,
                    null,
                    description
            );
        }

        // Si NO es base, la evidencia es obligatoria
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Este tipo de gasto requiere evidencia"
            );
        }

        validateFile(file);
        Double negativeValue = natureValue(amount);

        return closingSpendGateway.saveSpend(
                closingId,
                spendTypeId,
                zonaId,
                negativeValue,
                file,
                description
        );
    }

    Double natureValue(Double amount){
        return amount * -1;
    }

    public ClosingSpendResponseDto updateSpend(
            Long spendId,
            Long spendTypeId,
            Double amount,
            MultipartFile file,
            String description
    ) throws IOException {

        // Obtener el gasto existente
        ClosingSpendResponseDto existingSpend = closingSpendGateway.getSpendById(spendId);

        if (existingSpend == null) {
            throw new IllegalArgumentException("El registro de gasto no existe");
        }

        // Determinar si el usuario subió un nuevo archivo
        boolean hasNewFile = file != null && !file.isEmpty();

        // Si hay un nuevo archivo, validarlo
        if (hasNewFile) {
            validateFile(file);
        }

        // Llamar al gateway para actualizar
        // El gateway se encargará de mantener el archivo existente si no hay uno nuevo
        Double negativeValue = natureValue(amount);
        return closingSpendGateway.updateSpend(
                spendId,
                spendTypeId,
                negativeValue,
                file,
                description
        );
    }

    public void deactivateSpend(Long spendId) {
        closingSpendGateway.deactivateSpend(spendId);
    }

    public List<ClosingSpendResponseDto > getSpendsByClosingId(Long closingId) {

        if (closingGateway.getById(closingId) == null) {
            throw new IllegalArgumentException("El cierre no existe");
        }

        return closingSpendGateway.getSpendsByClosingId(closingId);
    }

    private void validateFile(MultipartFile file) {

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "El archivo supera el tamaño máximo permitido (5MB)"
            );
        }

        if (!file.getContentType().startsWith("image/")
                && !file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido"
            );
        }
    }
    public ClosingSpendFileDto getFileSpend(Long spendId) {
        return closingSpendGateway.getFileBySpendId(spendId);
    }

}
