package com.recaudo.api.infrastructure.scheduler;

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

    //private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    private final OtherConceptsOrchestrator orchestrator;


    /**
     * Cálculo diario de conceptos adicionales (mora, GMF, seguros, etc.).
     * Cron: todos los días a las 23:00:00

    @Scheduled(cron = "0 0 23 * * *")
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
     */

    //@Scheduled(cron = "0 */5 * * * *")
    /*public void otherConceptsDailyJob() {

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

     */
}
