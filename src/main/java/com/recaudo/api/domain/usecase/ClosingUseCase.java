package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ClosingGateway;
import com.recaudo.api.domain.gateway.ClosingStatusGateway;
import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.model.dto.response.ClosingResponseDto;
import com.recaudo.api.domain.model.dto.response.TodayClosingProjection;
import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.ApproveClosingDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeClosingStatusDto;
import com.recaudo.api.domain.model.dto.rest_api.ClosingDto;
import com.recaudo.api.infrastructure.helper.util.ClosingStatus;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;


@AllArgsConstructor
@UseCase
public class ClosingUseCase {

    private ClosingGateway closingGateway;
    private PersonGateway personGateway;
    private UserGateway userGateway;
    private ClosingStatusGateway closingStatusGateway;


    public ClosingResponseDto getById(Long id){
        return closingGateway.getById(id);
    }

    public List<ClosingResponseDto> getBypersonId(Long id){
        UserDto person = userGateway.getById(id);
        return closingGateway.getByPersonId(person.getPersonId());
    }

    public TodayClosingProjection getTodayClosingByPerson(Long personId) {

        return closingGateway
                .getTodayClosingByPerson(personId)
                .orElse(null);
    }

    public TodayClosingProjection getTodayClosingByPersonAndZona(Long personId, Long zonaId) {

        return closingGateway
                .getTodayClosingByPersonAndZona(personId, zonaId)
                .orElse(null);
    }


    public ClosingResponseDto save(ClosingDto dto){
        return closingGateway.save(dto);
    }
    public ClosingResponseDto edit(Long id, ClosingDto dto){
        return closingGateway.edit(id,dto);
    }

    public ClosingResponseDto approveClosing(ApproveClosingDto dto) {

        // Validar que el cierre existe
        ClosingResponseDto closing = closingGateway.getById(dto.getClosingId());
        if (closing == null) {
            throw new IllegalArgumentException("El cierre no existe");
        }

        // Validar tipo de entrega
        if (!Arrays.asList("admin", "asesor", "parcial").contains(dto.getDeliveryType())) {
            throw new IllegalArgumentException("Tipo de entrega inválido");
        }

        // Validar montos según tipo de entrega
        Double amountAdmin = dto.getAmountAdmin() != null ? dto.getAmountAdmin() : 0.0;
        Double amountAsesor = dto.getAmountAsesor() != null ? dto.getAmountAsesor() : 0.0;

        switch (dto.getDeliveryType()) {
            case "admin":
                if (amountAdmin <= 0) {
                    throw new IllegalArgumentException("El monto para admin debe ser mayor a cero");
                }
                if (amountAsesor != 0) {
                    throw new IllegalArgumentException("Para entrega al admin, el monto del asesor debe ser cero");
                }
                break;

            case "asesor":
                if (amountAsesor <= 0) {
                    throw new IllegalArgumentException("El monto para asesor debe ser mayor a cero");
                }
                if (amountAdmin != 0) {
                    throw new IllegalArgumentException("Para entrega al asesor, el monto del admin debe ser cero");
                }
                break;

            case "parcial":
                if (amountAdmin <= 0 || amountAsesor <= 0) {
                    throw new IllegalArgumentException("Ambos montos deben ser mayores a cero para entrega parcial");
                }
                break;
        }

        // Actualizar el cierre con los montos de entrega
        ClosingResponseDto updatedClosing = closingGateway.updateDeliveryAmounts(
                dto.getClosingId(),
                dto.getDeliveryType(),
                amountAdmin,
                amountAsesor
        );

        // Cambiar estado a APPROVED
        ChangeClosingStatusDto statusDto = ChangeClosingStatusDto.builder()
                .closingId(dto.getClosingId())
                .newStatus(ClosingStatus.APPROVED)
                .build();

        closingStatusGateway.updateStatus(statusDto);

        return updatedClosing;
    }


}
