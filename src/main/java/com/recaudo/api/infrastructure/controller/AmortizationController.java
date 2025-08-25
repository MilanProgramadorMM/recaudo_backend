package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.AmortizationResponseDto;
import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import com.recaudo.api.domain.usecase.AmortizationUseCase;
import com.recaudo.api.domain.usecase.PeriodUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/amortization")
@RequiredArgsConstructor
public class AmortizationController {

    @Autowired
    AmortizationUseCase amortizationUseCase;

    @GetMapping
    public ResponseEntity<List<AmortizationResponseDto>> getAllPeriods() {
        return ResponseEntity.ok(amortizationUseCase.getAll());
    }
}
