package com.recaudo.api.domain.gateway.impl;

import com.recaudo.api.domain.model.dto.response.CreditRatingDTO;
import com.recaudo.api.domain.model.dto.response.CreditSnapshotProjection;
import com.recaudo.api.domain.model.entity.PortfolioSnapshotEntity;
import com.recaudo.api.infrastructure.repository.CreditRatingRangeRepository;
import com.recaudo.api.infrastructure.repository.PortfolioSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioSnapshotTransactionalOps {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final CreditRatingRangeRepository creditRatingRangeRepository;

    @Transactional
    public int limpiarSnapshotPrevio(LocalDate fecha) {
        int eliminados = snapshotRepository.deleteBySnapshotDate(fecha);
        if (eliminados > 0) {
            log.info("[PortfolioSnapshot] Se eliminaron {} registros previos para fecha={} (re-ejecución)",
                    eliminados, fecha);
        }
        return eliminados;
    }

    @Transactional
    public int procesarPagina(List<CreditSnapshotProjection> creditos, LocalDate fecha, String jobExecutionId) {
        LocalDateTime ahora = LocalDateTime.now();

        List<PortfolioSnapshotEntity> entidades = creditos.stream()
                .map(p -> mapToEntity(p, fecha, jobExecutionId, ahora))
                .collect(Collectors.toList());

        snapshotRepository.saveAll(entidades);
        return entidades.size();
    }

    private PortfolioSnapshotEntity mapToEntity(
            CreditSnapshotProjection p, LocalDate fecha, String jobExecutionId, LocalDateTime ahora) {

        CreditRatingDTO calificacion = calcularCalificacion(p.getDiasMora());

        return PortfolioSnapshotEntity.builder()
                .snapshotDate(fecha)
                .creditId(p.getCreditId())
                .personId(p.getPersonId())
                .clienteFullname(p.getCliente())
                .clienteDocumento(p.getDocumento())
                .zonaId(p.getZonaId())
                .zonaNombre(p.getZona())
                .estadoCredito(PortfolioSnapshotEntity.CreditStatus.valueOf(p.getEstadoCredito()))
                .creditLineId(p.getCreditLineId())
                .creditLineNombre(p.getCreditLineNombre())
                .periodId(p.getPeriodId())
                .periodNombre(p.getPeriodNombre())
                .periodCodigo(p.getPeriodCodigo())
                .taxTypeId(p.getTaxTypeId())
                .taxTypeNombre(p.getTaxTypeNombre())
                .taxValue(p.getTaxValue())
                .cuotasPlaneadas(p.getCuotasPlaneadas())
                .totalCuotas(p.getTotalCuotas())
                .cuotasPagadas(p.getCuotasPagadas())
                .cuotasPendientes(p.getCuotasPendientes())
                .capitalGenerado(p.getCapitalGenerado())
                .capitalPagado(p.getCapitalPagado())
                .capitalPendiente(p.getCapitalPendiente())
                .interesGenerado(p.getInteresGenerado())
                .interesPagado(p.getInteresPagado())
                .interesPendiente(p.getInteresPendiente())
                .seguroVidaGenerado(p.getSeguroVidaGenerado())
                .seguroVidaPagado(p.getSeguroVidaPagado())
                .seguroVidaPendiente(p.getSeguroVidaPendiente())
                .seguroCarteraGenerado(p.getSeguroCarteraGenerado())
                .seguroCarteraPagado(p.getSeguroCarteraPagado())
                .seguroCarteraPendiente(p.getSeguroCarteraPendiente())
                .moraGenerada(p.getMoraGenerada())
                .moraPagada(p.getMoraPagada())
                .moraPendiente(p.getMoraPendiente())
                .diasMora(p.getDiasMora())
                .totalPagado(p.getTotalPagado())
                .saldoTotal(p.getSaldoTotal())
                .otrosConceptosGenerado(p.getOtrosConceptosGenerado())
                .ratingValue(calificacion.getRatingValue())
                .ratingRangeStart(calificacion.getStart())
                .ratingRangeEnd(calificacion.getEnd())
                .createdAt(ahora)
                .jobExecutionId(jobExecutionId)
                .build();
    }

    private CreditRatingDTO calcularCalificacion(Integer diasMora) {
        if (diasMora == null || diasMora < 0) diasMora = 0;

        return creditRatingRangeRepository.findByDiasMora(diasMora)
                .map(r -> new CreditRatingDTO(r.getRatingValue(), r.getStart(), r.getEnd()))
                .orElse(new CreditRatingDTO("N/A", null, null));
    }
}