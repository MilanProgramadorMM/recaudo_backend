package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.AmortizationGateway;
import com.recaudo.api.domain.gateway.PeriodGateway;
import com.recaudo.api.domain.model.dto.response.AmortizationResponseDto;
import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import com.recaudo.api.infrastructure.repository.AmortizationRepository;
import com.recaudo.api.infrastructure.repository.PeriodRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AmortizationAdapter implements AmortizationGateway {

    AmortizationRepository amortizationRepository;

     @Override
     public List<AmortizationResponseDto> getAll() {
        return amortizationRepository.findAll()
                .stream()
                .map(data -> AmortizationResponseDto.builder()
                        .cod(data.getCod())
                        .name(data.getName())
                        .procedure(data.getProceAlmacRelacionado())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }
}
