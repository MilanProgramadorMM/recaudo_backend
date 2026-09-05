package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.model.entity.DashboardDebidoCobrarEntity;
import com.recaudo.api.domain.model.entity.ZonaEntity;
import com.recaudo.api.infrastructure.adapter.UserDetailsImpl;
import com.recaudo.api.infrastructure.repository.DashboardDebidoCobrarRepository;
import com.recaudo.api.infrastructure.repository.DashboardMetricsRepository;
import com.recaudo.api.infrastructure.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardDebidoCobrarOrchestrator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ZonaRepository zonaRepository;
    private final DashboardMetricsRepository metricsRepository;
    private final DashboardDebidoCobrarRepository debidoCobrarRepository;

    /**
     * Calcula e inserta el "debido a cobrar" por zona para la fecha indicada.
     * Elimina el registro previo del día antes de insertar (idempotencia).
     *
     */
    public void run(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.plusDays(1).atStartOfDay();

        log.info("[DebidoCobrar] Calculando para fecha {} — rango [{} , {})",
                fecha, inicio.format(FORMATTER), fin.format(FORMATTER));

        List<ZonaEntity> zonas = zonaRepository.findByStatusTrueOrderByOrderZonaAsc();

        if (zonas.isEmpty()) {
            log.warn("[DebidoCobrar] No se encontraron zonas activas.");
            return;
        }

        log.info("[DebidoCobrar] Zonas activas encontradas: {}", zonas.size());

        int exitosos = 0;
        int fallidos = 0;

        for (ZonaEntity zona : zonas) {
            try {
                // Eliminar registro previo del día para esta zona (idempotencia)
                //debidoCobrarRepository.deleteByZonaIdAndFecha(zona.getId(), inicio, fin);

                // Calcular el debido a cobrar
                BigDecimal valor = metricsRepository
                        .getTotalDebidoCobrarValorCuotaPendiente(inicio, fin, zona.getId());

                // Insertar el nuevo registro
                DashboardDebidoCobrarEntity entity = DashboardDebidoCobrarEntity.builder()
                        .value(valor)
                        .zonaId(zona.getId())
                        .userCreate("SYSTEM")
                        .createdAt(LocalDateTime.now())
                        .valueOriginal(valor)
                        .build();

                debidoCobrarRepository.save(entity);

                log.info("[DebidoCobrar]  Zona {} — valor: {}", zona.getId(), valor);
                exitosos++;

            } catch (Exception e) {
                log.error("[DebidoCobrar] Error en zona {}: {}",
                        zona.getId(), e.getMessage(), e);
                fallidos++;
            }
        }

        log.info("[DebidoCobrar] Finalizado — exitosos: {}, fallidos: {}", exitosos, fallidos);
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}