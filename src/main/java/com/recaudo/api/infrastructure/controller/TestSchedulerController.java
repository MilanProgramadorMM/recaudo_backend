package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.gateway.impl.DashboardDebidoCobrarOrchestrator;
import com.recaudo.api.domain.gateway.impl.OtherConceptsOrchestrator;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
@RestController
@RequestMapping("/dev/other-concepts")
@AllArgsConstructor
public class TestSchedulerController {

    private final OtherConceptsOrchestrator orchestrator;
    private final DashboardDebidoCobrarOrchestrator debidoCobrarOrchestrator;


    /**
     * Dispara el job para una fecha específica.
     * Ejemplo: POST /dev/other-concepts/run?fecha=2026-04-25
     */
    @PostMapping("/run")
    public ResponseEntity<String> run(
            @RequestParam(name = "fecha", required = false)
            String fechaStr) {

        LocalDate fecha = fechaStr != null
                ? LocalDate.parse(fechaStr)
                : LocalDate.now();

        orchestrator.runAll(fecha);

        return ResponseEntity.ok(
                "Job ejecutado para fecha: " + fecha);
    }


    @PostMapping("/run-deb")
    public ResponseEntity<String> runDebidoCobrar(
            @RequestParam(name = "fecha", required = false) String fechaStr) {

        LocalDate fecha = fechaStr != null
                ? LocalDate.parse(fechaStr)
                : LocalDate.now();

        debidoCobrarOrchestrator.run(fecha);

        return ResponseEntity.ok(
                "Job de debido a cobrar ejecutado para fecha: " + fecha);
    }

}
