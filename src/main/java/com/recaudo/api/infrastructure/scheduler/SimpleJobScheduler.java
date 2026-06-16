package com.recaudo.api.infrastructure.scheduler;

import com.recaudo.api.domain.gateway.impl.DashboardDebidoCobrarOrchestrator;
import com.recaudo.api.domain.gateway.impl.OtherConceptsOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleJobScheduler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OtherConceptsOrchestrator orchestrator;
    private final DashboardDebidoCobrarOrchestrator debidoCobrarOrchestrator;


    /**
     * Cálculo diario de conceptos adicionales (mora, GMF, seguros, etc.).
     * Cron: se configura en application.yml (app.scheduler.cron)
     */
    @Scheduled(cron = "${app.scheduler.cron.interes-mora}")
    public void otherConceptsDailyJob() {
        log.info("[Job] Iniciando cálculo de conceptos adicionales — {}",
                LocalDateTime.now().format(FORMATTER));
        try {
            orchestrator.runAll(LocalDate.now());
            log.info("[Job] Finalizado exitosamente — {}",
                    LocalDateTime.now().format(FORMATTER));
        } catch (Exception e) {
            log.error("[Job] Error durante la ejecución: {}", e.getMessage(), e);
        }
    }


    @Scheduled(cron = "${app.scheduler.cron.debido-cobrar}")
    public void debidoCobrarDailyJob() {
        log.info("[Job][DebidoCobrar] Iniciando — {}",
                LocalDateTime.now().format(FORMATTER));
        try {
            debidoCobrarOrchestrator.run(LocalDate.now());
            log.info("[Job][DebidoCobrar] Finalizado exitosamente — {}",
                    LocalDateTime.now().format(FORMATTER));
        } catch (Exception e) {
            log.error("[Job][DebidoCobrar] Error durante la ejecución: {}",
                    e.getMessage(), e);
        }
    }
}
