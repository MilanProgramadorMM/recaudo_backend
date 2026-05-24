package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.consultas.DashboardSummaryDto;
import com.recaudo.api.domain.usecase.DashboardMetricsUseCase;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/dashboard")
@AllArgsConstructor
public class DashboardMetricsController {

    private final DashboardMetricsUseCase dashboardMetricsUseCase;

    @GetMapping("/summary")
    public ResponseEntity<
            DefaultResponseDto<DashboardSummaryDto>
            > getSummary(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam
            Long zonaId
    ) {

        DashboardSummaryDto data =
                dashboardMetricsUseCase.getDashboardSummary(
                        fechaInicio,
                        fechaFin,
                        zonaId
                );

        return ResponseEntity.ok(
                DefaultResponseDto
                        .<DashboardSummaryDto>builder()
                        .message("Dashboard consultado")
                        .status(HttpStatus.OK)
                        .details("Resumen generado correctamente")
                        .data(data)
                        .build()
        );
    }

}
