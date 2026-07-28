package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.model.dto.response.CreditSnapshotProjection;
import com.recaudo.api.domain.model.dto.response.PortfolioSnapshotSummary;
import com.recaudo.api.domain.model.entity.PortfolioSnapshotEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.CreditSnapshotSourceRepository;
import com.recaudo.api.infrastructure.repository.PortfolioSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioSnapshotOrchestrator {

    private static final int PAGE_SIZE = 500;

    private final CreditSnapshotSourceRepository sourceRepository;
    private final PortfolioSnapshotTransactionalOps transactionalOps; // ← inyectado, no self-call

    public PortfolioSnapshotSummary run(LocalDate fecha) {
        String jobExecutionId = UUID.randomUUID().toString();

        log.info("[PortfolioSnapshot] Iniciando snapshot para fecha={} jobExecutionId={}",
                fecha, jobExecutionId);

        transactionalOps.limpiarSnapshotPrevio(fecha);

        int totalProcesados = 0;
        int pagina = 0;
        Date fechaSql = Date.valueOf(fecha);
        Page<CreditSnapshotProjection> page;

        do {
            Pageable pageable = PageRequest.of(pagina, PAGE_SIZE);
            page = sourceRepository.findActiveCreditsSnapshot(fechaSql, pageable);

            if (!page.getContent().isEmpty()) {
                try {
                    int insertados = transactionalOps.procesarPagina(page.getContent(), fecha, jobExecutionId);
                    totalProcesados += insertados;
                    log.info("[PortfolioSnapshot] Página {} procesada — {} créditos insertados (acumulado: {})",
                            pagina, insertados, totalProcesados);
                } catch (Exception e) {
                    log.error("[PortfolioSnapshot] Error en página {} — ABORTANDO job completo. Motivo: {}",
                            pagina, e.getMessage(), e);
                    throw new BadRequestException(
                            "Snapshot abortado en página " + pagina + " para fecha " + fecha);
                }
            }

            pagina++;

        } while (page.hasNext());

        log.info("[PortfolioSnapshot] Finalizado exitosamente fecha={} — procesados={} jobExecutionId={}",
                fecha, totalProcesados, jobExecutionId);

        return PortfolioSnapshotSummary.builder()
                .fecha(fecha)
                .totalCreditosProcesados(totalProcesados)
                .paginasConError(0)
                .jobExecutionId(jobExecutionId)
                .build();
    }
}