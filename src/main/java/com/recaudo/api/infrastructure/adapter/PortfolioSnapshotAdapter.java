package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PortfolioSnapshotIGateway;
import com.recaudo.api.domain.model.dto.response.CalificacionBucketDto;
import com.recaudo.api.domain.model.dto.response.ClientAggregateView;
import com.recaudo.api.domain.model.dto.response.ClientHistoryAnalysisDto;
import com.recaudo.api.domain.model.dto.response.ClientListItemDto;
import com.recaudo.api.domain.model.dto.response.ClientPortfolioStateDto;
import com.recaudo.api.domain.model.dto.response.ConceptoMontoDto;
import com.recaudo.api.domain.model.dto.response.ConteosCreditoDto;
import com.recaudo.api.domain.model.dto.response.CreditTransitionView;
import com.recaudo.api.domain.model.dto.response.CuotasResumenDto;
import com.recaudo.api.domain.model.dto.response.PortfolioSnapshotResponseDto;
import com.recaudo.api.domain.model.dto.response.PortfolioSnapshotView;
import com.recaudo.api.domain.model.dto.response.PuntoHistoricoClienteDto;
import com.recaudo.api.domain.model.dto.response.PuntoHistoricoDto;
import com.recaudo.api.domain.model.dto.response.ResumenEvolucionClienteDto;
import com.recaudo.api.domain.model.dto.response.ResumenEvolucionDto;
import com.recaudo.api.domain.model.dto.response.TransicionDiariaDto;
import com.recaudo.api.domain.model.dto.response.ZoneAggregateView;
import com.recaudo.api.domain.model.dto.response.ZoneHistoryAnalysisDto;
import com.recaudo.api.domain.model.dto.response.ZoneRatingView;
import com.recaudo.api.domain.model.dto.response.ZoneSnapshotStateDto;
import com.recaudo.api.exception.ResourceNotFoundException;
import com.recaudo.api.infrastructure.repository.PortfolioSnapshotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortfolioSnapshotAdapter implements PortfolioSnapshotIGateway {

    @Autowired
    private PortfolioSnapshotRepository portfolioSnapshotRepository;

    @Override
    public List<PortfolioSnapshotResponseDto> getBySnapshotDate(LocalDate fecha) {
        try {
            return portfolioSnapshotRepository.findBySnapshotDate(fecha).stream()
                    .map(this::mapView)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener los snapshots de cartera para la fecha: {}", fecha, e);
            throw new RuntimeException("Error al obtener los snapshots de cartera", e);
        }
    }

    @Override
    public ZoneSnapshotStateDto getZoneState(Long zoneId, LocalDate fecha) {
        try {
            List<ZoneAggregateView> agregados =
                    portfolioSnapshotRepository.findZoneAggregatesByDate(fecha, List.of(zoneId));

            if (agregados.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No existe snapshot de cartera para la zona " + zoneId + " en la fecha " + fecha);
            }

            List<CalificacionBucketDto> distribucion = mapRatingBuckets(
                    portfolioSnapshotRepository.findZoneRatingDistributionByDate(fecha, List.of(zoneId)));

            return mapZoneState(agregados.get(0), fecha, distribucion);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener el estado de la zona {} en la fecha {}", zoneId, fecha, e);
            throw new RuntimeException("Error al obtener el estado de la cartera de la zona", e);
        }
    }

    @Override
    public List<ZoneSnapshotStateDto> getZonesState(List<Long> zoneIds, LocalDate fecha) {
        try {
            List<ZoneAggregateView> agregados =
                    portfolioSnapshotRepository.findZoneAggregatesByDate(fecha, zoneIds);

            // Distribución de calificación agrupada por zona (una sola consulta)
            Map<Long, List<CalificacionBucketDto>> distribucionPorZona =
                    portfolioSnapshotRepository.findZoneRatingDistributionByDate(fecha, zoneIds).stream()
                            .collect(Collectors.groupingBy(
                                    ZoneRatingView::getZonaId,
                                    Collectors.mapping(
                                            r -> CalificacionBucketDto.builder()
                                                    .ratingValue(r.getRatingValue())
                                                    .cantidad(nzL(r.getCantidad()))
                                                    .build(),
                                            Collectors.toList())));

            return agregados.stream()
                    .map(a -> mapZoneState(
                            a, fecha, distribucionPorZona.getOrDefault(a.getZonaId(), List.of())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener el estado de las zonas {} en la fecha {}", zoneIds, fecha, e);
            throw new RuntimeException("Error al obtener el estado de la cartera de las zonas", e);
        }
    }

    @Override
    public ZoneHistoryAnalysisDto getZoneHistory(Long zoneId, LocalDate startDate, LocalDate endDate) {
        try {
            List<ZoneAggregateView> serieDiaria =
                    portfolioSnapshotRepository.findZoneDailyAggregates(zoneId, startDate, endDate);

            if (serieDiaria.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No existen snapshots de cartera para la zona " + zoneId
                                + " entre " + startDate + " y " + endDate);
            }

            List<PuntoHistoricoDto> serie = serieDiaria.stream()
                    .map(this::mapPunto)
                    .collect(Collectors.toList());

            List<TransicionDiariaDto> transiciones = calcularTransiciones(
                    portfolioSnapshotRepository.findCreditStatesForTransitions(zoneId, startDate, endDate));

            ResumenEvolucionDto resumen = calcularResumenEvolucion(serieDiaria, transiciones);

            return ZoneHistoryAnalysisDto.builder()
                    .zonaId(zoneId)
                    .zonaNombre(serieDiaria.get(serieDiaria.size() - 1).getZonaNombre())
                    .startDate(startDate)
                    .endDate(endDate)
                    .diasConSnapshot(serieDiaria.size())
                    .resumenEvolucion(resumen)
                    .serie(serie)
                    .transiciones(transiciones)
                    .build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener la evolución histórica de la zona {} entre {} y {}",
                    zoneId, startDate, endDate, e);
            throw new RuntimeException("Error al obtener la evolución histórica de la cartera de la zona", e);
        }
    }

    // ── mapeo: projection nativa (detalle) → DTO de respuesta ────────────────────
    private PortfolioSnapshotResponseDto mapView(PortfolioSnapshotView v) {
        PortfolioSnapshotResponseDto dto = new PortfolioSnapshotResponseDto();
        dto.setId(v.getId());
        dto.setSnapshotDate(v.getSnapshotDate());
        dto.setCreditId(v.getCreditId());
        dto.setPersonId(v.getPersonId());
        dto.setClienteFullname(v.getClienteFullname());
        dto.setClienteDocumento(v.getClienteDocumento());
        dto.setZonaId(v.getZonaId());
        dto.setZonaNombre(v.getZonaNombre());
        dto.setEstadoCredito(v.getEstadoCredito());
        dto.setCreditLineId(v.getCreditLineId());
        dto.setCreditLineNombre(v.getCreditLineNombre());
        dto.setPeriodId(v.getPeriodId());
        dto.setPeriodNombre(v.getPeriodNombre());
        dto.setPeriodCodigo(v.getPeriodCodigo());
        dto.setTaxTypeId(v.getTaxTypeId());
        dto.setTaxTypeNombre(v.getTaxTypeNombre());
        dto.setTaxValue(v.getTaxValue());
        dto.setCuotasPlaneadas(v.getCuotasPlaneadas());
        dto.setTotalCuotas(v.getTotalCuotas());
        dto.setCuotasPagadas(v.getCuotasPagadas());
        dto.setCuotasPendientes(v.getCuotasPendientes());
        dto.setCapitalGenerado(v.getCapitalGenerado());
        dto.setCapitalPagado(v.getCapitalPagado());
        dto.setCapitalPendiente(v.getCapitalPendiente());
        dto.setInteresGenerado(v.getInteresGenerado());
        dto.setInteresPagado(v.getInteresPagado());
        dto.setInteresPendiente(v.getInteresPendiente());
        dto.setSeguroVidaGenerado(v.getSeguroVidaGenerado());
        dto.setSeguroVidaPagado(v.getSeguroVidaPagado());
        dto.setSeguroVidaPendiente(v.getSeguroVidaPendiente());
        dto.setSeguroCarteraGenerado(v.getSeguroCarteraGenerado());
        dto.setSeguroCarteraPagado(v.getSeguroCarteraPagado());
        dto.setSeguroCarteraPendiente(v.getSeguroCarteraPendiente());
        dto.setMoraGenerada(v.getMoraGenerada());
        dto.setMoraPagada(v.getMoraPagada());
        dto.setMoraPendiente(v.getMoraPendiente());
        dto.setDiasMora(v.getDiasMora());
        dto.setTotalPagado(v.getTotalPagado());
        dto.setSaldoTotal(v.getSaldoTotal());
        dto.setOtrosConceptosGenerado(v.getOtrosConceptosGenerado());
        dto.setRatingValue(v.getRatingValue());
        dto.setRatingRangeStart(v.getRatingRangeStart());
        dto.setRatingRangeEnd(v.getRatingRangeEnd());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setJobExecutionId(v.getJobExecutionId());
        return dto;
    }

    // ── mapeo: agregado de zona → estado de cartera (endpoints 1 y 2) ────────────
    private ZoneSnapshotStateDto mapZoneState(
            ZoneAggregateView a, LocalDate fecha, List<CalificacionBucketDto> distribucion) {

        return ZoneSnapshotStateDto.builder()
                .fecha(fecha)
                .zonaId(a.getZonaId())
                .zonaNombre(a.getZonaNombre())
                .conteos(ConteosCreditoDto.builder()
                        .total(nzL(a.getTotalCreditos()))
                        .activos(nzL(a.getCreditosActivos()))
                        .cancelados(nzL(a.getCreditosCancelados()))
                        .inactivos(nzL(a.getCreditosInactivos()))
                        .enMora(nzL(a.getCreditosEnMora()))
                        .alDia(nzL(a.getCreditosAlDia()))
                        .build())
                .capital(concepto(a.getCapitalGenerado(), a.getCapitalPagado(), a.getCapitalPendiente()))
                .interes(concepto(a.getInteresGenerado(), a.getInteresPagado(), a.getInteresPendiente()))
                .seguroVida(concepto(a.getSeguroVidaGenerado(), a.getSeguroVidaPagado(), a.getSeguroVidaPendiente()))
                .seguroCartera(concepto(a.getSeguroCarteraGenerado(), a.getSeguroCarteraPagado(), a.getSeguroCarteraPendiente()))
                .mora(concepto(a.getMoraGenerada(), a.getMoraPagada(), a.getMoraPendiente()))
                .otrosConceptosGenerado(nz(a.getOtrosConceptosGenerado()))
                .totalPagado(nz(a.getTotalPagado()))
                .saldoTotal(nz(a.getSaldoTotal()))
                .cuotas(CuotasResumenDto.builder()
                        .planeadas(toLong(a.getCuotasPlaneadas()))
                        .totales(toLong(a.getTotalCuotas()))
                        .pagadas(toLong(a.getCuotasPagadas()))
                        .pendientes(toLong(a.getCuotasPendientes()))
                        .build())
                .diasMoraPromedio(nz(a.getDiasMoraPromedio()))
                .distribucionCalificacion(distribucion)
                .build();
    }

    // ── mapeo: agregado diario → punto de la serie temporal (endpoint 3) ─────────
    private PuntoHistoricoDto mapPunto(ZoneAggregateView a) {
        return PuntoHistoricoDto.builder()
                .fecha(a.getSnapshotDate())
                .totalCreditos(nzL(a.getTotalCreditos()))
                .creditosActivos(nzL(a.getCreditosActivos()))
                .creditosEnMora(nzL(a.getCreditosEnMora()))
                .creditosCancelados(nzL(a.getCreditosCancelados()))
                .saldoTotal(nz(a.getSaldoTotal()))
                .capitalPendiente(nz(a.getCapitalPendiente()))
                .interesPendiente(nz(a.getInteresPendiente()))
                .moraPendiente(nz(a.getMoraPendiente()))
                .capitalPagado(nz(a.getCapitalPagado()))
                .totalPagado(nz(a.getTotalPagado()))
                .cuotasPagadas(toLong(a.getCuotasPagadas()))
                .cuotasPendientes(toLong(a.getCuotasPendientes()))
                .diasMoraPromedio(nz(a.getDiasMoraPromedio()))
                .build();
    }

    private List<CalificacionBucketDto> mapRatingBuckets(List<ZoneRatingView> ratings) {
        return ratings.stream()
                .map(r -> CalificacionBucketDto.builder()
                        .ratingValue(r.getRatingValue())
                        .cantidad(nzL(r.getCantidad()))
                        .build())
                .collect(Collectors.toList());
    }

    // ── análisis temporal: transiciones entre snapshots consecutivos ─────────────
    private List<TransicionDiariaDto> calcularTransiciones(List<CreditTransitionView> estados) {
        // Agrupa el estado de cada crédito por fecha, en orden cronológico.
        Map<LocalDate, Map<Integer, CreditTransitionView>> porFecha = new TreeMap<>();
        for (CreditTransitionView e : estados) {
            porFecha.computeIfAbsent(e.getSnapshotDate(), k -> new HashMap<>())
                    .put(e.getCreditId(), e);
        }

        List<TransicionDiariaDto> resultado = new ArrayList<>();
        Map<Integer, CreditTransitionView> anterior = null;
        for (Map.Entry<LocalDate, Map<Integer, CreditTransitionView>> entrada : porFecha.entrySet()) {
            Map<Integer, CreditTransitionView> actual = entrada.getValue();
            if (anterior != null) {
                resultado.add(compararDia(entrada.getKey(), anterior, actual));
            }
            anterior = actual;
        }
        return resultado;
    }

    private TransicionDiariaDto compararDia(
            LocalDate fecha,
            Map<Integer, CreditTransitionView> anterior,
            Map<Integer, CreditTransitionView> actual) {

        long ingresaronMora = 0, salieronMora = 0, cambiaronEstado = 0,
                cancelados = 0, cambiosCalificacion = 0, nuevos = 0, salieron = 0;

        for (Map.Entry<Integer, CreditTransitionView> e : actual.entrySet()) {
            CreditTransitionView c = e.getValue();
            CreditTransitionView p = anterior.get(e.getKey());

            if (p == null) {
                nuevos++;
                continue;
            }

            boolean estabaEnMora = enMora(p);
            boolean estaEnMora = enMora(c);
            if (!estabaEnMora && estaEnMora) ingresaronMora++;
            if (estabaEnMora && !estaEnMora) salieronMora++;

            if (!Objects.equals(p.getEstadoCredito(), c.getEstadoCredito())) {
                cambiaronEstado++;
                if ("CANCELLED".equals(c.getEstadoCredito())
                        && !"CANCELLED".equals(p.getEstadoCredito())) {
                    cancelados++;
                }
            }

            if (!Objects.equals(p.getRatingValue(), c.getRatingValue())) {
                cambiosCalificacion++;
            }
        }

        for (Integer creditId : anterior.keySet()) {
            if (!actual.containsKey(creditId)) salieron++;
        }

        return TransicionDiariaDto.builder()
                .fecha(fecha)
                .ingresaronMora(ingresaronMora)
                .salieronMora(salieronMora)
                .cambiaronEstado(cambiaronEstado)
                .cancelados(cancelados)
                .cambiosCalificacion(cambiosCalificacion)
                .nuevosCreditos(nuevos)
                .creditosQueSalieron(salieron)
                .build();
    }

    // ── análisis temporal: resumen comparativo del período ───────────────────────
    private ResumenEvolucionDto calcularResumenEvolucion(
            List<ZoneAggregateView> serie, List<TransicionDiariaDto> transiciones) {

        ZoneAggregateView primero = serie.get(0);
        ZoneAggregateView ultimo = serie.get(serie.size() - 1);

        BigDecimal saldoInicial = nz(primero.getSaldoTotal());
        BigDecimal saldoFinal = nz(ultimo.getSaldoTotal());
        BigDecimal capitalPendIni = nz(primero.getCapitalPendiente());
        BigDecimal capitalPendFin = nz(ultimo.getCapitalPendiente());
        BigDecimal moraPendIni = nz(primero.getMoraPendiente());
        BigDecimal moraPendFin = nz(ultimo.getMoraPendiente());

        return ResumenEvolucionDto.builder()
                .fechaInicial(primero.getSnapshotDate())
                .fechaFinal(ultimo.getSnapshotDate())
                .saldoInicial(saldoInicial)
                .saldoFinal(saldoFinal)
                .variacionSaldo(saldoFinal.subtract(saldoInicial))
                .pctVariacionSaldo(porcentaje(saldoFinal.subtract(saldoInicial), saldoInicial))
                .capitalPendienteInicial(capitalPendIni)
                .capitalPendienteFinal(capitalPendFin)
                .variacionCapitalPendiente(capitalPendFin.subtract(capitalPendIni))
                .moraPendienteInicial(moraPendIni)
                .moraPendienteFinal(moraPendFin)
                .variacionMoraPendiente(moraPendFin.subtract(moraPendIni))
                .pctVariacionMoraPendiente(porcentaje(moraPendFin.subtract(moraPendIni), moraPendIni))
                .totalRecuperado(nz(ultimo.getTotalPagado()).subtract(nz(primero.getTotalPagado())))
                .capitalRecuperado(nz(ultimo.getCapitalPagado()).subtract(nz(primero.getCapitalPagado())))
                .creditosEnMoraInicial(nzL(primero.getCreditosEnMora()))
                .creditosEnMoraFinal(nzL(ultimo.getCreditosEnMora()))
                .variacionCreditosEnMora(nzL(ultimo.getCreditosEnMora()) - nzL(primero.getCreditosEnMora()))
                .totalIngresaronMora(transiciones.stream().mapToLong(TransicionDiariaDto::getIngresaronMora).sum())
                .totalSalieronMora(transiciones.stream().mapToLong(TransicionDiariaDto::getSalieronMora).sum())
                .totalCambiaronEstado(transiciones.stream().mapToLong(TransicionDiariaDto::getCambiaronEstado).sum())
                .totalCancelados(transiciones.stream().mapToLong(TransicionDiariaDto::getCancelados).sum())
                .totalCambiosCalificacion(transiciones.stream().mapToLong(TransicionDiariaDto::getCambiosCalificacion).sum())
                .totalNuevosCreditos(transiciones.stream().mapToLong(TransicionDiariaDto::getNuevosCreditos).sum())
                .totalCreditosQueSalieron(transiciones.stream().mapToLong(TransicionDiariaDto::getCreditosQueSalieron).sum())
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────────
    private ConceptoMontoDto concepto(BigDecimal generado, BigDecimal pagado, BigDecimal pendiente) {
        return ConceptoMontoDto.builder()
                .generado(nz(generado))
                .pagado(nz(pagado))
                .pendiente(nz(pendiente))
                .build();
    }

    private boolean enMora(CreditTransitionView v) {
        return v.getDiasMora() != null && v.getDiasMora() > 0;
    }

    private BigDecimal porcentaje(BigDecimal variacion, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return variacion.multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<ClientListItemDto> getClientsByDate(LocalDate fecha, String busqueda) {
        try {
            return portfolioSnapshotRepository.findClientsBySnapshotDate(fecha, busqueda).stream()
                    .map(v -> ClientListItemDto.builder()
                            .personId(v.getPersonId())
                            .clienteFullname(v.getClienteFullname())
                            .clienteDocumento(v.getClienteDocumento())
                            .totalCreditos(nzL(v.getTotalCreditos()))
                            .saldoTotal(nz(v.getSaldoTotal()))
                            .diasMoraMaximo(v.getDiasMoraMaximo())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al listar clientes para la fecha {}", fecha, e);
            throw new RuntimeException("Error al listar clientes con cartera", e);
        }
    }

    @Override
    public ClientPortfolioStateDto getClientState(Long personId, LocalDate fecha) {
        try {
            ClientAggregateView v = portfolioSnapshotRepository
                    .findClientAggregateByDate(fecha, personId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe snapshot de cartera para el cliente " + personId
                                    + " en la fecha " + fecha));

            return mapClientState(v, fecha);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener el estado del cliente {} en la fecha {}", personId, fecha, e);
            throw new RuntimeException("Error al obtener el estado de la cartera del cliente", e);
        }
    }

    @Override
    public ClientHistoryAnalysisDto getClientHistory(Long personId, LocalDate startDate, LocalDate endDate) {
        try {
            List<ClientAggregateView> serieDiaria =
                    portfolioSnapshotRepository.findClientDailyAggregates(personId, startDate, endDate);

            if (serieDiaria.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No existen snapshots de cartera para el cliente " + personId
                                + " entre " + startDate + " y " + endDate);
            }

            List<PuntoHistoricoClienteDto> serie = serieDiaria.stream()
                    .map(this::mapPuntoCliente)
                    .collect(Collectors.toList());

            ResumenEvolucionClienteDto resumen = calcularResumenEvolucionCliente(serieDiaria);

            return ClientHistoryAnalysisDto.builder()
                    .personId(personId)
                    .clienteFullname(serieDiaria.get(serieDiaria.size() - 1).getClienteFullname())
                    .startDate(startDate)
                    .endDate(endDate)
                    .diasConSnapshot(serieDiaria.size())
                    .resumenEvolucion(resumen)
                    .serie(serie)
                    .build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener la evolución histórica del cliente {} entre {} y {}",
                    personId, startDate, endDate, e);
            throw new RuntimeException("Error al obtener la evolución histórica de la cartera del cliente", e);
        }
    }

    // ── mapeo: agregado de cliente → estado de cartera puntual ───────────────────
    private ClientPortfolioStateDto mapClientState(ClientAggregateView a, LocalDate fecha) {
        List<String> zonas = a.getZonaNombres() != null
                ? Arrays.asList(a.getZonaNombres().split(",\\s*"))
                : List.of();

        return ClientPortfolioStateDto.builder()
                .fecha(fecha)
                .personId(a.getPersonId())
                .clienteFullname(a.getClienteFullname())
                .clienteDocumento(a.getClienteDocumento())
                .zonas(zonas)
                .conteos(ConteosCreditoDto.builder()
                        .total(nzL(a.getTotalCreditos()))
                        .activos(nzL(a.getCreditosActivos()))
                        .cancelados(nzL(a.getCreditosCancelados()))
                        .inactivos(nzL(a.getCreditosInactivos()))
                        .enMora(nzL(a.getCreditosEnMora()))
                        .alDia(nzL(a.getCreditosAlDia()))
                        .build())
                .capital(concepto(a.getCapitalGenerado(), a.getCapitalPagado(), a.getCapitalPendiente()))
                .interes(concepto(a.getInteresGenerado(), a.getInteresPagado(), a.getInteresPendiente()))
                .seguroVida(concepto(a.getSeguroVidaGenerado(), a.getSeguroVidaPagado(), a.getSeguroVidaPendiente()))
                .seguroCartera(concepto(a.getSeguroCarteraGenerado(), a.getSeguroCarteraPagado(), a.getSeguroCarteraPendiente()))
                .mora(concepto(a.getMoraGenerada(), a.getMoraPagada(), a.getMoraPendiente()))
                .otrosConceptosGenerado(nz(a.getOtrosConceptosGenerado()))
                .totalPagado(nz(a.getTotalPagado()))
                .saldoTotal(nz(a.getSaldoTotal()))
                .cuotas(CuotasResumenDto.builder()
                        .planeadas(toLong(a.getCuotasPlaneadas()))
                        .totales(toLong(a.getTotalCuotas()))
                        .pagadas(toLong(a.getCuotasPagadas()))
                        .pendientes(toLong(a.getCuotasPendientes()))
                        .build())
                .diasMoraMaximo(a.getDiasMoraMaximo())
                .diasMoraPromedio(nz(a.getDiasMoraPromedio()))
                .build();
    }

    // ── mapeo: agregado diario de cliente → punto de la serie ────────────────────
    private PuntoHistoricoClienteDto mapPuntoCliente(ClientAggregateView a) {
        return PuntoHistoricoClienteDto.builder()
                .fecha(a.getSnapshotDate())
                .totalCreditos(nzL(a.getTotalCreditos()))
                .creditosActivos(nzL(a.getCreditosActivos()))
                .creditosEnMora(nzL(a.getCreditosEnMora()))
                .creditosCancelados(nzL(a.getCreditosCancelados()))
                .saldoTotal(nz(a.getSaldoTotal()))
                .capitalPendiente(nz(a.getCapitalPendiente()))
                .interesPendiente(nz(a.getInteresPendiente()))
                .moraPendiente(nz(a.getMoraPendiente()))
                .capitalPagado(nz(a.getCapitalPagado()))
                .totalPagado(nz(a.getTotalPagado()))
                .cuotasPagadas(toLong(a.getCuotasPagadas()))
                .cuotasPendientes(toLong(a.getCuotasPendientes()))
                .diasMoraMaximo(a.getDiasMoraMaximo())
                .diasMoraPromedio(nz(a.getDiasMoraPromedio()))
                .build();
    }

    // ── resumen: comparación entre primer y último día del rango, para un cliente ─
    private ResumenEvolucionClienteDto calcularResumenEvolucionCliente(List<ClientAggregateView> serie) {
        ClientAggregateView primero = serie.get(0);
        ClientAggregateView ultimo = serie.get(serie.size() - 1);

        BigDecimal saldoInicial = nz(primero.getSaldoTotal());
        BigDecimal saldoFinal = nz(ultimo.getSaldoTotal());
        BigDecimal capitalPendIni = nz(primero.getCapitalPendiente());
        BigDecimal capitalPendFin = nz(ultimo.getCapitalPendiente());
        BigDecimal moraPendIni = nz(primero.getMoraPendiente());
        BigDecimal moraPendFin = nz(ultimo.getMoraPendiente());

        return ResumenEvolucionClienteDto.builder()
                .fechaInicial(primero.getSnapshotDate())
                .fechaFinal(ultimo.getSnapshotDate())
                .saldoInicial(saldoInicial)
                .saldoFinal(saldoFinal)
                .variacionSaldo(saldoFinal.subtract(saldoInicial))
                .pctVariacionSaldo(porcentaje(saldoFinal.subtract(saldoInicial), saldoInicial))
                .capitalPendienteInicial(capitalPendIni)
                .capitalPendienteFinal(capitalPendFin)
                .variacionCapitalPendiente(capitalPendFin.subtract(capitalPendIni))
                .moraPendienteInicial(moraPendIni)
                .moraPendienteFinal(moraPendFin)
                .variacionMoraPendiente(moraPendFin.subtract(moraPendIni))
                .totalRecuperado(nz(ultimo.getTotalPagado()).subtract(nz(primero.getTotalPagado())))
                .capitalRecuperado(nz(ultimo.getCapitalPagado()).subtract(nz(primero.getCapitalPagado())))
                .creditosEnMoraInicial(nzL(primero.getCreditosEnMora()))
                .creditosEnMoraFinal(nzL(ultimo.getCreditosEnMora()))
                .variacionCreditosEnMora(nzL(ultimo.getCreditosEnMora()) - nzL(primero.getCreditosEnMora()))
                .build();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Long nzL(Long v) {
        return v == null ? 0L : v;
    }

    private Long toLong(BigDecimal v) {
        return v == null ? 0L : v.longValue();
    }

}
