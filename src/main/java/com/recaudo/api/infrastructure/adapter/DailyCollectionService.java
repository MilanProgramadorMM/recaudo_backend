package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.infrastructure.repository.CreditRatingRangeRepository;
import com.recaudo.api.infrastructure.repository.DailyCollectionRepository;
import com.recaudo.api.infrastructure.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyCollectionService {

    @Autowired
    private DailyCollectionRepository dailyCollectionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CreditRatingRangeRepository creditRatingRangeRepository;


    public DailyCollectionResultDTO getDailyCollection(String username, Long personId, LocalDate date) {
        List<Long> zonas = personRepository.getZonasIdByAsesor(personId);

        // ── LISTA 1: cobro hoy ──
        List<DailyCollectionProjection> dailyData = dailyCollectionRepository.findDailyCollection(zonas, date);

        List<Long> creditIdsHoy = dailyData.stream().map(DailyCollectionProjection::getCreditId).distinct().toList();
        List<DailyCollectionRespaldoProjection> recaudosHoy = creditIdsHoy.isEmpty()
                ? List.of() : dailyCollectionRepository.finDailyCollectionRespaldo(creditIdsHoy);
        Map<Long, List<DailyCollectionRespaldoProjection>> mapaHoy = recaudosHoy.stream()
                .collect(Collectors.groupingBy(DailyCollectionRespaldoProjection::getCreditId));

        List<DailyCollectionDTO> cobroHoy = dailyData.stream()
                .map(b -> DailyCollectionDTO.builder()
                        .data(mapCobroHoy(b))
                        .recaudos(mapaHoy.getOrDefault(b.getCreditId(), List.of()))
                        .ratingCredit(calcularCalificacion(b.getPeriodosVencidos()))
                        .build())
                .toList();

        // ── LISTA 2: cartera ──
        List<PortfolioProjection> portfolio = dailyCollectionRepository.findPortfolio(zonas, date);
        List<Long> creditIdsCartera = portfolio.stream().map(PortfolioProjection::getCreditId).distinct().toList();
        List<DailyCollectionRespaldoProjection> recaudosCartera = creditIdsCartera.isEmpty()
                ? List.of() : dailyCollectionRepository.finDailyCollectionRespaldo(creditIdsCartera);
        Map<Long, List<DailyCollectionRespaldoProjection>> mapaCartera = recaudosCartera.stream()
                .collect(Collectors.groupingBy(DailyCollectionRespaldoProjection::getCreditId));

        List<DailyCollectionDTO> carteraZona = portfolio.stream()
                .map(b -> DailyCollectionDTO.builder()
                        .data(mapCartera(b))
                        .recaudos(mapaCartera.getOrDefault(b.getCreditId(), List.of()))
                        .ratingCredit(new CreditRatingDTO("N/A", null, null)) // sin mora aún
                        .build())
                .toList();

        // ── LISTA 3: mora ──
        List<OverdueProjection> overdue = dailyCollectionRepository.findOverdue(zonas);
        List<Long> creditIdsMora = overdue.stream().map(OverdueProjection::getCreditId).distinct().toList();
        List<DailyCollectionRespaldoProjection> recaudosMora = creditIdsMora.isEmpty()
                ? List.of() : dailyCollectionRepository.finDailyCollectionRespaldo(creditIdsMora);
        Map<Long, List<DailyCollectionRespaldoProjection>> mapaMora = recaudosMora.stream()
                .collect(Collectors.groupingBy(DailyCollectionRespaldoProjection::getCreditId));

        List<DailyCollectionDTO> enMora = overdue.stream()
                .map(b -> DailyCollectionDTO.builder()
                        .data(mapMora(b))
                        .recaudos(mapaMora.getOrDefault(b.getCreditId(), List.of()))
                        .ratingCredit(calcularCalificacion(b.getPeriodosVencidos()))
                        .build())
                .toList();

        return DailyCollectionResultDTO.builder()
                .cobroHoy(cobroHoy)
                .carteraZona(carteraZona)
                .enMora(enMora)
                .build();
    }

    private CardDataDTO mapCobroHoy(DailyCollectionProjection b) {
        return CardDataDTO.builder()
                .creditId(b.getCreditId())
                .clientName(b.getClientName())
                .clientOrden(b.getClientOrden())
                .zona(b.getZona())
                .totalCapitalValue(b.getTotalCapitalValue())
                .saldoPendiente(b.getSaldoPendiente())
                .totalMoraCredito(b.getTotalMoraCredito())
                .periodosVencidos(b.getPeriodosVencidos())
                .fechaCredito(b.getFechaCredito())
                .lineaname(b.getLineaName())
                .periodo(b.getPeriodo())
                .plazoCredito(b.getPlazoCredito())
                .fechaVence(b.getFechaVence())
                .totalCuotas(b.getTotalCuotas())
                .cuotasPagadas(b.getCuotasPagadas())
                .cuotasVencidas(b.getCuotasVencidas())
                .direccion(b.getDireccion())
                .whatsapp(b.getWhatsapp())
                .celular(b.getCelular())
                .barrio(b.getBarrio())
                .municipio(b.getMunicipio())
                .cuotaId(b.getCuotaId())
                .quotaNumber(b.getQuotaNumber())
                .expirationDate(b.getExpirationDate())
                .valorCuota(b.getValorCuota())
                .saldoPendienteCuota(b.getSaldoPendienteCuota())
                .interestMora(b.getInterestMora())
                .paidToday(b.getPaidToday())
                .paidFull(b.getPaidFull())
                .liquidated(b.getLiquidated())
                .paymentPromiseDate(b.getPaymentPromiseDate())
                .noPago(b.getNoPago())
                .noPagoReason(b.getNoPagoReason())
                .nombreDia(b.getNombreDia())
                .build();
    }

    private CardDataDTO mapCartera(PortfolioProjection b) {
        return CardDataDTO.builder()
                .creditId(b.getCreditId())
                .clientName(b.getClientName())
                .clientOrden(b.getClientOrden())
                .zonaCode(b.getZonaCode())
                .zona(b.getZona())
                .totalCapitalValue(b.getTotalCapitalValue())
                .saldoPendiente(b.getSaldoPendiente())
                .totalMoraCredito(BigDecimal.ZERO)
                .periodosVencidos(0)
                .fechaCredito(b.getFechaCredito())
                .lineaname(b.getLineaname())
                .periodo(b.getPeriodo())
                .plazoCredito(b.getPlazoCredito())
                .fechaVence(b.getFechaVence())
                .totalCuotas(b.getTotalCuotas())
                .cuotasPagadas(b.getCuotasPagadas())
                .cuotasVencidas(b.getCuotasVencidas())
                .direccion(b.getDireccion())
                .whatsapp(b.getWhatsapp())
                .celular(b.getCelular())
                .barrio(b.getBarrio())
                .municipio(b.getMunicipio())
                .cuotaId(b.getProximaCuotaId())
                .quotaNumber(b.getProximaCuotaNumero())
                .cuotasPendientes(b.getCuotasPendientes())
                .proximaCuotaFecha(b.getProximaCuotaFecha())
                .proximaCuotaNumero(b.getProximaCuotaNumero())
                .build();
    }

    private CardDataDTO mapMora(OverdueProjection b) {
        return CardDataDTO.builder()
                .creditId(b.getCreditId())
                .clientName(b.getClientName())
                .clientOrden(b.getClientOrden())
                .zonaCode(b.getZonaCode())
                .zona(b.getZona())
                .totalCapitalValue(b.getTotalCapitalValue())
                .saldoPendiente(b.getSaldoPendiente())
                .totalMoraCredito(b.getTotalMoraCredito())
                .periodosVencidos(b.getPeriodosVencidos())
                .fechaCredito(b.getFechaCredito())
                .lineaname(b.getLineaname())
                .periodo(b.getPeriodo())
                .plazoCredito(b.getPlazoCredito())
                .fechaVence(b.getFechaVence())
                .totalCuotas(b.getTotalCuotas())
                .cuotasPagadas(b.getCuotasPagadas())
                .cuotasVencidas(b.getCuotasVencidas())
                .direccion(b.getDireccion())
                .whatsapp(b.getWhatsapp())
                .celular(b.getCelular())
                .barrio(b.getBarrio())
                .municipio(b.getMunicipio())
                .primeraCuotaVencida(b.getPrimeraCuotaVencida())
                .primeraCuotaVencidaNumero(b.getPrimeraCuotaVencidaNumero())
                .cuotaId(b.getPrimeraCuotaVencidaId())
                .build();
    }

    private CreditRatingDTO calcularCalificacion(Integer diasMora) {
        if (diasMora == null || diasMora < 0) diasMora = 0;
        return creditRatingRangeRepository.findByDiasMora(diasMora)
                .map(r -> new CreditRatingDTO(r.getRatingValue(), r.getStart(), r.getEnd()))
                .orElse(new CreditRatingDTO("N/A", null, null));
    }
}
