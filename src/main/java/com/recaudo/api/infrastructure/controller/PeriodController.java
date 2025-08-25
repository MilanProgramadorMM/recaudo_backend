package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.PeriodResponseDto;
import com.recaudo.api.domain.usecase.PeriodUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/periods")
@RequiredArgsConstructor
public class PeriodController {

    @Autowired
    PeriodUseCase periodUseCase;

    @GetMapping
    public ResponseEntity<List<PeriodResponseDto>> getAllPeriods() {
        return ResponseEntity.ok(periodUseCase.getAll());
    }
}
