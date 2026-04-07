package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.consultas.DebidoCobrarDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DefaultConsultasDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleCreditosPorZona;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleMovimientoPorZonaDTO;
import com.recaudo.api.domain.model.dto.response.consultas.DetalleSaldoVencidoDTO;
import com.recaudo.api.domain.model.dto.response.consultas.MovimientoPorZonaDTO;
import com.recaudo.api.domain.usecase.ConsultasUseCase;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/consultas")
@AllArgsConstructor
public class ConsultasController {

    private ConsultasUseCase consultasUseCase;

    @GetMapping("/movimientos/all")
    public ResponseEntity<List<MovimientoPorZonaDTO>> movimientosPorZona(
            @RequestParam(required = false) Long concept,
            @RequestParam(required = false) Long paymentType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getMovimientosPorZona(concept, paymentType, startDate, endDate)
        );
    }

    @GetMapping("/movimientos/detalle/{zona}")
    public ResponseEntity<List<DetalleMovimientoPorZonaDTO>> detalleMovimientosZona(
            @PathVariable("zona") Long zona,
            @RequestParam(required = false) Long concept,
            @RequestParam(required = false) Long paymentType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getDetalleMovimientosPorZona(concept, paymentType, startDate, endDate, zona)
        );
    }

    @GetMapping("/saldos-vencidos")
    public ResponseEntity<List<DefaultConsultasDTO>> saldosVencidosZona(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getSaldoVencidoPorZona(startDate, endDate)
        );
    }

    @GetMapping("/saldos-vencidos/{zona}")
    public ResponseEntity<List<DetalleSaldoVencidoDTO>> detalleSaldosVencidosZona(
            @PathVariable("zona") Long zona,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getDetalleSaldoVencidoPorZona(startDate, endDate, zona)
        );
    }

    @GetMapping("/creditos")
    public ResponseEntity<List<DefaultConsultasDTO>> creditosPorZona(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getCreditosPorZona(startDate, endDate)
        );
    }

    @GetMapping("/creditos/{zona}")
    public ResponseEntity<List<DetalleCreditosPorZona>> detalleCreditosPorZona(
            @PathVariable("zona") Long zona,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getDetalleCreditosPorZona(startDate, endDate, zona)
        );
    }

    @GetMapping("/debido-cobrar")
    public ResponseEntity<List<DebidoCobrarDTO>> debidoCobrarPorZona(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                consultasUseCase.getDebidoCobrarPorZona(startDate, endDate)
        );
    }

}
