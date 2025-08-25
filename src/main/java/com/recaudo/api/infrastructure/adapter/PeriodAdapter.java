package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PeriodGateway;
import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import com.recaudo.api.infrastructure.repository.PeriodRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PeriodAdapter implements PeriodGateway {

     PeriodRepository periodRepository;

     @Override
     public List<PeriodResponseDto> getAll() {
        return periodRepository.findAll()
                .stream()
                .map(period -> PeriodResponseDto.builder()
                        .cod(period.getCod())
                        .name(period.getName())
                        .description(period.getDescription())
                        .factorConversion(period.getFactorConversion())
                        .status(period.isStatus())
                        .build())
                .toList();
     }
}
