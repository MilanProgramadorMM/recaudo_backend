package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.rest_api.RecaudoRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.ReverseRecaudoRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.ReverseCapitalInterestRequestDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewRecaudoAdapter {

    private static final String KEY_TIPCON      = "TIPCON";
    private static final String GLO_CAPITAL     = "VIV";
    private static final String GLO_INTERES     = "VIT";
    private static final String GLO_SEG_VIDA    = "SV";
    private static final String GLO_SEG_CARTERA = "SC";
    private static final String GLO_MORA        = "IMT";

    private final NewRecaudoRepository                 recaudoRepository;
    private final GlotypesRepository                   glotypesRepository;
    private final ConceptRepository                    conceptRepository;
    private final RecaudoDetailRepository              recaudoDetailRepository;
    private final CreditAmortizationNRepository        amortizationRepository;
    private final CreditAmortizationDetailRepository   amortizationDetailRepository;
    private final CreditRepository                     creditRepository;
    private final PeriodRepository                     periodRepository;
    private final CollectionVisitAdapter               collectionVisitAdapter;
    private final ClosingAdapter                       closingAdapter;
    private final CreditOtherConceptsRepository        otherConceptsRepository;
    private final CreditOtherConceptDetailRepository   otherConceptDetailRepository;

    @Transactional
    public CreditRecaudoStatusDto getCreditPaymentStatus(Long creditId) {
        try {
            CreditEntity credit = creditRepository.findById(creditId)
                    .orElseThrow(() -> new RuntimeException("Crédito no encontrado: " + creditId));

            String periodName = periodRepository.findById(credit.getPeriodId())
                    .map(PeriodEntity::getName)
                    .orElse("N/A");

            List<CreditAmortizationNEntity> cuotas =
                    amortizationRepository.findByCreditIdOrderByQuotaNumberAsc(creditId);

            if (cuotas.isEmpty()) {
                throw new RuntimeException("No se encontraron cuotas para el crédito: " + creditId);
            }

            List<NewRecaudoEntity> recaudos = recaudoRepository.findByCreditId(creditId);

            // Pre-fetch all data for optimization
            OptimizationContext ctx = buildOptimizationContext(creditId, cuotas, recaudos);

            long cuotasPagadas    = cuotas.stream().filter(c -> "S".equals(c.getPaidFull())).count();
            long cuotasPendientes = cuotas.stream().filter(c -> "N".equals(c.getPaidFull())).count();

            BigDecimal totalPagado = recaudos.stream()
                    .map(NewRecaudoEntity::getValuePaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // totalPending() incluye mora causada − mora pagada
            BigDecimal totalPendiente = cuotas.stream()
                    .filter(c -> "N".equals(c.getPaidFull()))
                    .map(cuota -> resolveQuotaComponents(cuota, ctx).totalPending())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredito = Optional.ofNullable(credit.getTotalIntentionValue())
                    .orElse(BigDecimal.ZERO);

            BigDecimal porcentajePagado = BigDecimal.ZERO;
            if (totalCredito.compareTo(BigDecimal.ZERO) > 0) {
                porcentajePagado = totalPagado
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalCredito, 2, RoundingMode.HALF_UP);
            }

            BigDecimal tasaCredito = credit.getTaxValue()
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            LocalDate today = LocalDate.now();

            List<QuotaDetailDto> cuotasDetail = cuotas.stream()
                    .map(cuota -> buildQuotaDetail(cuota, tasaCredito, today, ctx))
                    .collect(Collectors.toList());

            List<RecaudoDetailDto> recaudosDetail = recaudos.stream()
                    .map(r -> buildRecaudoDetail(r, ctx))
                    .collect(Collectors.toList());

            log.info("Estado de pago obtenido: creditId={}, cuotasPagadas={}, cuotasPendientes={}, porcentaje={}%",
                    creditId, cuotasPagadas, cuotasPendientes, porcentajePagado);

            return CreditRecaudoStatusDto.builder()
                    .creditId(creditId)
                    .personId(credit.getPersonId())
                    .quotaValue(cuotas.get(0).getTotalQuotaValue())
                    .periodQuantity(credit.getPeriodQuantity())
                    .periodName(periodName)
                    .totalIntentionValue(credit.getTotalIntentionValue())
                    .totalInterestValue(credit.getTotalInterestValue())
                    .totalCapitalValue(credit.getTotalCapitalValue())
                    .taxValue(credit.getTaxValue())
                    .stationery(credit.getStationery())
                    .totalCuotas(cuotas.size())
                    .cuotasPagadas((int) cuotasPagadas)
                    .cuotasPendientes((int) cuotasPendientes)
                    .totalPagado(totalPagado)
                    .totalPendiente(totalPendiente)
                    .porcentajePagado(porcentajePagado)
                    .cuotas(cuotasDetail)
                    .recaudos(recaudosDetail)
                    .build();

        } catch (Exception e) {
            log.error("Error al consultar estado de pago del crédito ID: {}", creditId, e);
            throw new RuntimeException("Error al consultar estado de pago: " + e.getMessage(), e);
        }
    }

    private OptimizationContext buildOptimizationContext(
            Long creditId,
            List<CreditAmortizationNEntity> cuotas,
            List<NewRecaudoEntity> recaudos) {

        List<Long> quotaIds = cuotas.stream().map(CreditAmortizationNEntity::getId).collect(Collectors.toList());
        List<Long> recaudoIds = recaudos.stream().map(NewRecaudoEntity::getId).collect(Collectors.toList());

        Map<String, Long> glotypes = glotypesRepository.findByKey(KEY_TIPCON).stream()
                .collect(Collectors.toMap(GlotypesEntity::getCode, GlotypesEntity::getId, (a, b) -> a));

        Map<Long, List<CreditAmortizationDetailEntity>> amorDetailsByQuota =
                amortizationDetailRepository.findByAmortizationIdIn(quotaIds).stream()
                        .collect(Collectors.groupingBy(CreditAmortizationDetailEntity::getAmortizationId));

        Map<Long, List<RecaudoDetailEntity>> recDetailsByRecaudo =
                recaudoDetailRepository.findByRecaudoIdIn(recaudoIds).stream()
                        .collect(Collectors.groupingBy(RecaudoDetailEntity::getRecaudoId));

        Map<Long, Long> recToQuotaMap = recaudos.stream()
                .filter(r -> r.getQuotaId() != null)
                .collect(Collectors.toMap(NewRecaudoEntity::getId, NewRecaudoEntity::getQuotaId, (a, b) -> a));

        Map<Long, List<RecaudoDetailEntity>> recDetailsByQuota = recDetailsByRecaudo.entrySet().stream()
                .filter(e -> recToQuotaMap.containsKey(e.getKey()))
                .collect(Collectors.toMap(
                        e -> recToQuotaMap.get(e.getKey()),
                        java.util.Map.Entry::getValue,
                        (list1, list2) -> {
                            java.util.List<RecaudoDetailEntity> combined = new java.util.ArrayList<>(list1);
                            combined.addAll(list2);
                            return combined;
                        }
                ));

        List<CreditOtherConceptsEntity> otherConcepts = otherConceptsRepository.findByCreditId(creditId);
        Map<Long, Integer> otherIdToQuotaNumber = otherConcepts.stream()
                .collect(Collectors.toMap(CreditOtherConceptsEntity::getId, CreditOtherConceptsEntity::getQuotaNumber, (a, b) -> a));

        Map<Integer, List<CreditOtherConceptDetailEntity>> otherDetailsByQuotaNumber =
                otherConceptDetailRepository.findAllByCreditId(creditId).stream()
                        .filter(d -> otherIdToQuotaNumber.containsKey(d.getCreditOtherConceptId()))
                        .collect(Collectors.groupingBy(d -> otherIdToQuotaNumber.get(d.getCreditOtherConceptId())));

        Map<Long, Integer> quotaNumberByQuotaId = cuotas.stream()
                .collect(Collectors.toMap(CreditAmortizationNEntity::getId, CreditAmortizationNEntity::getQuotaNumber, (a, b) -> a));

        return OptimizationContext.builder()
                .glotypes(glotypes)
                .amorDetailsByQuota(amorDetailsByQuota)
                .recDetailsByRecaudo(recDetailsByRecaudo)
                .recDetailsByQuota(recDetailsByQuota)
                .otherDetailsByQuotaNumber(otherDetailsByQuotaNumber)
                .quotaNumberByQuotaId(quotaNumberByQuotaId)
                .build();
    }

    private QuotaDetailDto buildQuotaDetail(
            CreditAmortizationNEntity cuota,
            BigDecimal tasaCredito,
            LocalDate today,
            OptimizationContext ctx) {

        QuotaComponentsDto comp = resolveQuotaComponents(cuota, ctx);
        BigDecimal saldoPendiente = comp.totalPending();

        boolean overdue = cuota.getExpirationDate().isBefore(today)
                && "N".equals(cuota.getPaidFull());

        int daysOverdue = 0;
        if (overdue) {
            daysOverdue = (int) ChronoUnit.DAYS.between(cuota.getExpirationDate(), today);
        }

        Long glotypeIdMora = ctx.glotypes.get(GLO_MORA);

        List<CreditOtherConceptDetailEntity> otherDetails = ctx.otherDetailsByQuotaNumber
                .getOrDefault(cuota.getQuotaNumber(), java.util.Collections.emptyList());

        BigDecimal moraAcumulada = otherDetails.stream()
                .filter(d -> Objects.equals(d.getConceptId(), glotypeIdMora))
                .map(CreditOtherConceptDetailEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long diasMoraRegistrados = otherDetails.stream()
                .filter(d -> Objects.equals(d.getConceptId(), glotypeIdMora))
                .count();

        boolean moraSaldada = diasMoraRegistrados > 0
                && moraAcumulada.compareTo(BigDecimal.ZERO) == 0;

        return QuotaDetailDto.builder()
                .quotaId(cuota.getId())
                .quotaNumber(cuota.getQuotaNumber())
                .expirationDate(String.valueOf(cuota.getExpirationDate()))
                .liquidated(cuota.getLiquidated())
                .paidFull(cuota.getPaidFull())
                .quotaValue(cuota.getTotalQuotaValue())
                .totalPaid(comp.totalPaid())
                .remainingBalance(saldoPendiente)
                .portfolioInsurancePending(comp.pendingPortfolioInsurance())
                .lifeInsurancePending(comp.pendingLifeInsurance())
                .interestPending(comp.pendingInterest())
                .investmentPending(comp.pendingCapital())
                .totalPending(saldoPendiente)
                .isPaid("S".equals(cuota.getPaidFull()))
                .delayPenalty(moraAcumulada)
                .daysOverdue(daysOverdue)
                .hasInterestPayment("S".equals(cuota.getLiquidated()))
                .isOverdue(overdue)
                .moraAcumulada(moraAcumulada)
                .moraSaldada(moraSaldada)
                .diasMoraRegistrados(diasMoraRegistrados)
                .build();
    }

    private RecaudoDetailDto buildRecaudoDetail(NewRecaudoEntity recaudo, OptimizationContext ctx) {
        Integer quotaNumber = null;
        if (recaudo.getQuotaId() != null) {
            quotaNumber = ctx.quotaNumberByQuotaId.get(recaudo.getQuotaId().longValue());
        }

        List<RecaudoDetailEntity> detalles = ctx.recDetailsByRecaudo
                .getOrDefault(recaudo.getId(), java.util.Collections.emptyList());

        Long idCapital = ctx.glotypes.get(GLO_CAPITAL);
        Long idInteres = ctx.glotypes.get(GLO_INTERES);
        Long idSegVida = ctx.glotypes.get(GLO_SEG_VIDA);
        Long idSegCart = ctx.glotypes.get(GLO_SEG_CARTERA);
        Long idMora    = ctx.glotypes.get(GLO_MORA);

        BigDecimal investmentValue    = sumDetailByConcept(detalles, idCapital);
        BigDecimal interestValue      = sumDetailByConcept(detalles, idInteres);
        BigDecimal lifeInsurance      = sumDetailByConcept(detalles, idSegVida);
        BigDecimal portfolioInsurance = sumDetailByConcept(detalles, idSegCart);
        BigDecimal delayPenalty       = sumDetailByConcept(detalles, idMora);

        return RecaudoDetailDto.builder()
                .recaudoId(recaudo.getId().longValue())
                .quotaNumber(quotaNumber)
                .conceptName("RECAUDO EN RUTA")
                .valuePaid(recaudo.getValuePaid())
                .investmentValue(investmentValue)
                .interestValue(interestValue)
                .lifeInsurance(lifeInsurance)
                .portfolioInsurance(portfolioInsurance)
                .delayPenalty(delayPenalty)
                .userCreate(recaudo.getUserCreate())
                .createdAt(recaudo.getCreatedAt() != null
                        ? recaudo.getCreatedAt().toString()
                        : null)
                .build();
    }

    private BigDecimal sumDetailByConcept(List<RecaudoDetailEntity> detalles, Long conceptId) {
        if (detalles == null || conceptId == null) return BigDecimal.ZERO;
        return detalles.stream()
                .filter(d -> conceptId.equals(d.getConceptId()))
                .map(RecaudoDetailEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<RecaudoResponseProjection> getRecaudosByUserAndDate(
            Long closingId, LocalDate fecha, Long zonaId) {

        log.info("Consultando recaudos: closing={} fecha={} zona={}", closingId, fecha, zonaId);
        List<RecaudoResponseProjection> result =
                recaudoRepository.findRecaudosWithClientName(closingId, fecha, zonaId);

        if (result.isEmpty()) {
            log.warn("Sin recaudos para closing={} fecha={} zona={}", closingId, fecha, zonaId);
        }
        return result;
    }

    public List<CreditIntentionResponseProjection> getIntentionsByUserAndDate(
            Long closingId, LocalDate fecha, Long zonaId) {

        log.info("Consultando intenciones: closing={} fecha={} zona={}", closingId, fecha, zonaId);
        List<CreditIntentionResponseProjection> result =
                recaudoRepository.findIntentionsByUserAndDate(closingId, fecha, zonaId);

        if (result.isEmpty()) {
            log.warn("Sin intenciones para closing={} fecha={} zona={}", closingId, fecha, zonaId);
        }
        return result;
    }

    //  PROCESAMIENTO DE PAGO
    @Transactional
    public RecaudoResultDto processPayment(
            RecaudoRequestDto requestDto,
            MultipartFile file,
            Long personId,
            String token) throws Exception {

        if (requestDto.getValuePaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor pagado debe ser mayor a cero");
        }

        validateClosingStatus(requestDto, personId, token);

        String distributionType = Optional.ofNullable(requestDto.getDistributionType())
                .orElse("NORMAL");

        switch (distributionType) {
            case "RECAUDO_CAPITAL":
            case "RECAUDO_INTERESES":
                return processSingleComponentPayment(requestDto, file, distributionType);
            case "NORMAL":
            case "RECAUDO_RUTA":
            case "AJUSTE_PERDIDA":
                return processMultipleQuotasPayment(requestDto, file, distributionType);
            default:
                throw new IllegalArgumentException("Tipo de distribución inválido: " + distributionType);
        }
    }

    // Pago a un solo componente
    private RecaudoResultDto processSingleComponentPayment(
            RecaudoRequestDto requestDto,
            MultipartFile file,
            String distributionType) throws Exception {

        ConceptEntity conceptNC = conceptRepository.findByConceptKey("NC")
                .orElseThrow(() -> new RuntimeException("Concepto NC no encontrado"));

        String gloCode = "RECAUDO_CAPITAL".equals(distributionType) ? GLO_CAPITAL : GLO_INTERES;
        Long glotypeId = resolveGlotypeId(gloCode);

        NewRecaudoEntity recaudo = buildRecaudoHeader(
                requestDto, file, null, requestDto.getValuePaid());
        recaudo = recaudoRepository.save(recaudo);

        saveDetail(recaudo.getId(), conceptNC.getId(), glotypeId,
                requestDto.getValuePaid().negate());

        log.info("Pago componente {} procesado: crédito={}", distributionType,
                requestDto.getCreditId());

        return RecaudoResultDto.builder()
                .creditId(requestDto.getCreditId())
                .totalPaid(requestDto.getValuePaid())
                .cuotasPagadas(0).cuotasFaltantes(0).saldoSobrante(BigDecimal.ZERO)
                .build();
    }

    // Pago normal / ruta / ajuste
    private RecaudoResultDto processMultipleQuotasPayment(
            RecaudoRequestDto requestDto,
            MultipartFile file,
            String distributionType) throws Exception {

        String conceptKey;
        switch (distributionType) {
            case "AJUSTE_PERDIDA": conceptKey = "AC"; break;
            case "RECAUDO_RUTA":   conceptKey = "RR"; break;
            default:               conceptKey = "NC"; break;
        }

        ConceptEntity concepto = conceptRepository.findByConceptKey(conceptKey)
                .orElseThrow(() -> new RuntimeException("Concepto no encontrado: " + conceptKey));

        // Resolver IDs de glotypes una sola vez, fuera del loop
        Long idCapital = resolveGlotypeId(GLO_CAPITAL);
        Long idInteres = resolveGlotypeId(GLO_INTERES);
        Long idSegVida = resolveGlotypeId(GLO_SEG_VIDA);
        Long idSegCart = resolveGlotypeId(GLO_SEG_CARTERA);
        Long idMora    = resolveGlotypeId(GLO_MORA);

        List<CreditAmortizationNEntity> cuotasPendientes =
                amortizationRepository.findByCreditIdAndPaidFullOrderByQuotaNumberAsc(
                        requestDto.getCreditId(), "N");

        if (cuotasPendientes.isEmpty()) {
            throw new RuntimeException("No hay cuotas pendientes para el crédito: "
                    + requestDto.getCreditId());
        }

        BigDecimal saldoRestante = requestDto.getValuePaid();
        int cuotasLiquidadas = 0;
        String username = getUsernameToken();

        for (CreditAmortizationNEntity cuota : cuotasPendientes) {
            if (saldoRestante.compareTo(BigDecimal.ZERO) <= 0) break;

            collectionVisitAdapter.registerDailyVisitIfNotExists(
                    requestDto.getCreditId(), cuota.getId(), username, LocalDate.now());

            // Incluye mora causada y mora pagada
            QuotaComponentsDto comp = resolveQuotaComponents(cuota);

            if (comp.totalPending().compareTo(BigDecimal.ZERO) <= 0) {
                cuota.setLiquidated("S");
                cuota.setPaidFull("S");
                amortizationRepository.save(cuota);
                collectionVisitAdapter.markAsPaidToday(cuota.getId(), LocalDate.now());
                continue;
            }

            PaymentDistribution dist = distributePaymentNormal(comp, saldoRestante);

            if (dist.getTotalApplied().compareTo(BigDecimal.ZERO) <= 0) continue;

            // Cabecera del recaudo
            NewRecaudoEntity recaudo =
                    buildRecaudoHeader(requestDto, file, cuota.getId(), dist.getTotalApplied());
            recaudo = recaudoRepository.save(recaudo);

            // ── Detalles por componente (negativos = salida de caja) ──────────
            saveDetailIfNonZero(recaudo.getId(), concepto.getId(),
                    idSegCart, dist.getPortfolioInsuranceApplied().negate());
            saveDetailIfNonZero(recaudo.getId(), concepto.getId(),
                    idSegVida, dist.getLifeInsuranceApplied().negate());
            saveDetailIfNonZero(recaudo.getId(), concepto.getId(),
                    idInteres, dist.getInterestApplied().negate());
            saveDetailIfNonZero(recaudo.getId(), concepto.getId(),
                    idCapital, dist.getInvestmentApplied().negate());
            // Si la cuota tiene mora y el cliente abonó a ella, se registra
            // en el detalle con glotype IMT — igual que los demás conceptos
            otherConceptsRepository.findByCreditIdAndQuotaNumber(
                    cuota.getCreditId(), cuota.getQuotaNumber()
            ).ifPresent(
                    creditOtherConceptsEntity -> saveOtherConceptsDetail(
                            creditOtherConceptsEntity.getId(),
                            idMora,
                            dist.getMoraApplied().negate()
                    )
            );

            // Estado de la cuota
            if (dist.getInterestApplied().compareTo(BigDecimal.ZERO) > 0) {
                cuota.setLiquidated("S");
            }

            // paid_full = 'S' solo cuando se cubre totalQuotaValue + moraCausada
            BigDecimal nuevoTotalPagado = comp.totalPaid().add(dist.getTotalApplied());
            if (nuevoTotalPagado.compareTo(comp.totalDebt()) >= 0) {
                cuota.setPaidFull("S");
                cuotasLiquidadas++;
                collectionVisitAdapter.markAsPaidToday(cuota.getId(), LocalDate.now());
            }

            amortizationRepository.save(cuota);
            saldoRestante = saldoRestante.subtract(dist.getTotalApplied());
        }

        log.info("Pago {} procesado. cuotasLiquidadas={}, saldoSobrante={}",
                distributionType, cuotasLiquidadas, saldoRestante.max(BigDecimal.ZERO));

        return RecaudoResultDto.builder()
                .creditId(requestDto.getCreditId())
                .totalPaid(requestDto.getValuePaid())
                .cuotasPagadas(cuotasLiquidadas)
                .cuotasFaltantes(cuotasPendientes.size() - cuotasLiquidadas)
                .saldoSobrante(saldoRestante.max(BigDecimal.ZERO))
                .build();
    }

    //  REVERSIONES
    @Transactional
    public RecaudoResultDto reverseRecaudos(ReverseRecaudoRequestDto requestDto) {
        try {
            if (requestDto.getRecaudoIds() == null || requestDto.getRecaudoIds().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un recaudo a reversar");
            }

            ConceptEntity conceptND = conceptRepository.findByConceptKey("ND")
                    .orElseThrow(() -> new RuntimeException("Concepto ND no encontrado"));

            BigDecimal totalReversado = BigDecimal.ZERO;
            int recaudosReversados = 0;
            String username = getUsernameToken();

            for (Long recaudoId : requestDto.getRecaudoIds()) {

                NewRecaudoEntity original = recaudoRepository.findById(recaudoId)
                        .orElseThrow(() -> new RuntimeException("Recaudo no encontrado: " + recaudoId));

                if (!original.getCreditId().equals(requestDto.getCreditId())) {
                    throw new IllegalArgumentException(
                            "El recaudo " + recaudoId + " no pertenece al crédito especificado");
                }

                List<RecaudoDetailEntity> originalDetails =
                        recaudoDetailRepository.findByRecaudoId(recaudoId);

                NewRecaudoEntity reversa = NewRecaudoEntity.builder()
                        .creditId(original.getCreditId())
                        .quotaId(original.getQuotaId())
                        .valuePaid(original.getValuePaid().abs())
                        .paymentTypeId(original.getPaymentTypeId())
                        .bankId(original.getBankId())
                        .accountNumber(original.getAccountNumber())
                        .userCreate(username)
                        .createdAt(LocalDateTime.now())
                        .build();
                reversa = recaudoRepository.save(reversa);

                // Invierte todos los detalles del recaudo original,
                // incluido mora (IMT) si el cliente había abonado a ella
                for (RecaudoDetailEntity det : originalDetails) {
                    recaudoDetailRepository.save(RecaudoDetailEntity.builder()
                            .recaudoId(reversa.getId())
                            .typeRecaudoId(conceptND.getId())
                            .conceptId(det.getConceptId())
                            .value(det.getValue().negate())
                            .build());
                }

                if (original.getQuotaId() != null) {
                    updateQuotaStatusAfterReversal(original.getQuotaId());
                }

                totalReversado = totalReversado.add(original.getValuePaid());
                recaudosReversados++;
            }

            log.info("Reversión completada: {} recaudos reversados, total={}",
                    recaudosReversados, totalReversado);

            return RecaudoResultDto.builder()
                    .creditId(requestDto.getCreditId())
                    .totalPaid(totalReversado)
                    .cuotasPagadas(0).cuotasFaltantes(0).saldoSobrante(BigDecimal.ZERO)
                    .build();

        } catch (Exception e) {
            log.error("Error al reversar recaudos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reversar recaudos: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RecaudoResultDto reverseCapital(ReverseCapitalInterestRequestDto requestDto) {
        try {
            return reverseComponentPayment(requestDto, resolveGlotypeId(GLO_CAPITAL), "capital");
        } catch (Exception e) {
            log.error("Error al reversar capital: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reversar capital: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RecaudoResultDto reverseInterest(ReverseCapitalInterestRequestDto requestDto) {
        try {
            return reverseComponentPayment(requestDto, resolveGlotypeId(GLO_INTERES), "interés");
        } catch (Exception e) {
            log.error("Error al reversar intereses: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reversar intereses: " + e.getMessage(), e);
        }
    }

    private RecaudoResultDto reverseComponentPayment(
            ReverseCapitalInterestRequestDto requestDto,
            Long targetGlotypeId,
            String componentLabel) {

        if (requestDto.getRecaudoIds() == null || requestDto.getRecaudoIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un pago de " + componentLabel + " a reversar");
        }

        ConceptEntity conceptND = conceptRepository.findByConceptKey("ND")
                .orElseThrow(() -> new RuntimeException("Concepto ND no encontrado"));

        BigDecimal totalReversado = BigDecimal.ZERO;
        String username = getUsernameToken();

        for (Long recaudoId : requestDto.getRecaudoIds()) {

            NewRecaudoEntity original = recaudoRepository.findById(recaudoId)
                    .orElseThrow(() -> new RuntimeException("Recaudo no encontrado: " + recaudoId));

            if (!original.getCreditId().equals(requestDto.getCreditId())) {
                throw new IllegalArgumentException("El recaudo no pertenece al crédito especificado");
            }

            List<RecaudoDetailEntity> details =
                    recaudoDetailRepository.findByRecaudoId(recaudoId);

            BigDecimal componentValue = sumDetailByGlotypeAbs(details, targetGlotypeId);
            if (componentValue.compareTo(BigDecimal.ZERO) <= 0) continue;

            NewRecaudoEntity reversa = NewRecaudoEntity.builder()
                    .creditId(original.getCreditId())
                    .quotaId(original.getQuotaId())
                    .valuePaid(componentValue)
                    .paymentTypeId(original.getPaymentTypeId())
                    .bankId(original.getBankId())
                    .accountNumber(original.getAccountNumber())
                    .userCreate(username)
                    .createdAt(LocalDateTime.now())
                    .build();
            reversa = recaudoRepository.save(reversa);

            saveDetail(reversa.getId(), conceptND.getId(), targetGlotypeId, componentValue);

            if (original.getQuotaId() != null) {
                updateQuotaStatusAfterReversal(original.getQuotaId());
            }

            totalReversado = totalReversado.add(componentValue);
        }

        log.info("Reversión de {} completada. total={}", componentLabel, totalReversado);

        return RecaudoResultDto.builder()
                .creditId(requestDto.getCreditId())
                .totalPaid(totalReversado)
                .cuotasPagadas(0).cuotasFaltantes(0).saldoSobrante(BigDecimal.ZERO)
                .build();
    }

    //  DISTRIBUCIÓN DE PAGO
    /**
     * Prioridad de aplicación:
     *   1. Mora             ← penalidad se salda primero
     *   2. Seguro de cartera
     *   3. Seguro de vida
     *   4. Intereses
     *   5. Capital
     *
     * Si la cuota no tiene mora causada, pendingMora() = 0 y el paso 1
     * no consume nada del saldo disponible.
     */
    private PaymentDistribution distributePaymentNormal(
            QuotaComponentsDto comp, BigDecimal available) {

        PaymentDistribution dist = new PaymentDistribution();
        BigDecimal remaining = available;

        // 1. Mora (intereses moratorios acumulados)
        BigDecimal moraApplied = remaining.min(comp.pendingMora());
        dist.setMoraApplied(moraApplied);
        remaining = remaining.subtract(moraApplied);

        // 2. Seguro de cartera
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal portfolioApplied = remaining.min(comp.pendingPortfolioInsurance());
            dist.setPortfolioInsuranceApplied(portfolioApplied);
            remaining = remaining.subtract(portfolioApplied);
        }

        // 3. Seguro de vida
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lifeApplied = remaining.min(comp.pendingLifeInsurance());
            dist.setLifeInsuranceApplied(lifeApplied);
            remaining = remaining.subtract(lifeApplied);
        }

        // 4. Intereses
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal interestApplied = remaining.min(comp.pendingInterest());
            dist.setInterestApplied(interestApplied);
            remaining = remaining.subtract(interestApplied);
        }

        // 5. Capital
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal capitalApplied = remaining.min(comp.pendingCapital());
            dist.setInvestmentApplied(capitalApplied);
            remaining = remaining.subtract(capitalApplied);
        }

        dist.setTotalApplied(available.subtract(remaining));
        return dist;
    }

    private QuotaComponentsDto resolveQuotaComponents(CreditAmortizationNEntity cuota) {
        return resolveQuotaComponents(cuota, null);
    }

    private QuotaComponentsDto resolveQuotaComponents(CreditAmortizationNEntity cuota, OptimizationContext ctx) {

        Long idCapital = ctx != null ? ctx.glotypes.get(GLO_CAPITAL) : resolveGlotypeId(GLO_CAPITAL);
        Long idInteres = ctx != null ? ctx.glotypes.get(GLO_INTERES) : resolveGlotypeId(GLO_INTERES);
        Long idSegVida = ctx != null ? ctx.glotypes.get(GLO_SEG_VIDA) : resolveGlotypeId(GLO_SEG_VIDA);
        Long idSegCart = ctx != null ? ctx.glotypes.get(GLO_SEG_CARTERA) : resolveGlotypeId(GLO_SEG_CARTERA);
        Long idMora    = ctx != null ? ctx.glotypes.get(GLO_MORA) : resolveGlotypeId(GLO_MORA);

        List<CreditAmortizationDetailEntity> amorDetails;
        if (ctx != null) {
            amorDetails = ctx.amorDetailsByQuota.getOrDefault(cuota.getId(), java.util.Collections.emptyList());
        } else {
            amorDetails = amortizationDetailRepository.findByAmortizationId(cuota.getId());
        }

        BigDecimal capital = sumAmortDetailByConcept(amorDetails, idCapital);
        BigDecimal interes = sumAmortDetailByConcept(amorDetails, idInteres);
        BigDecimal segVida = sumAmortDetailByConcept(amorDetails, idSegVida);
        BigDecimal segCart = sumAmortDetailByConcept(amorDetails, idSegCart);

        BigDecimal moraCausada;
        if (ctx != null) {
            moraCausada = ctx.otherDetailsByQuotaNumber
                    .getOrDefault(cuota.getQuotaNumber(), java.util.Collections.emptyList()).stream()
                    .filter(d -> Objects.equals(d.getConceptId(), idMora))
                    .map(CreditOtherConceptDetailEntity::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            moraCausada = otherConceptDetailRepository
                    .sumByCreditAndQuotaAndConcept(cuota.getCreditId(), cuota.getQuotaNumber(), idMora);
        }

        BigDecimal capitalPaid, interesPaid, segVidaPaid, segCartPaid, moraPaid;
        if (ctx != null) {
            List<RecaudoDetailEntity> recDetails = ctx.recDetailsByQuota
                    .getOrDefault(cuota.getId(), java.util.Collections.emptyList());
            capitalPaid = sumDetailByConcept(recDetails, idCapital).abs();
            interesPaid = sumDetailByConcept(recDetails, idInteres).abs();
            segVidaPaid = sumDetailByConcept(recDetails, idSegVida).abs();
            segCartPaid = sumDetailByConcept(recDetails, idSegCart).abs();
            moraPaid    = sumDetailByConcept(recDetails, idMora).abs();
        } else {
            capitalPaid = recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idCapital).abs();
            interesPaid = recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idInteres).abs();
            segVidaPaid = recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idSegVida).abs();
            segCartPaid = recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idSegCart).abs();
            moraPaid    = recaudoDetailRepository.sumByQuotaIdAndConceptId(cuota.getId(), idMora).abs();
        }

        return QuotaComponentsDto.builder()
                .capital(capital)              .capitalPaid(capitalPaid)
                .interest(interes)             .interestPaid(interesPaid)
                .lifeInsurance(segVida)        .lifeInsurancePaid(segVidaPaid)
                .portfolioInsurance(segCart)   .portfolioInsurancePaid(segCartPaid)
                .moraCausada(moraCausada)      .moraPaid(moraPaid)
                .totalQuotaValue(cuota.getTotalQuotaValue())
                .build();
    }

    //  HELPERS
    /**
     * Recalcula paid_full y liquidated tras una reversión.
     * Usa comp.totalDebt() para contemplar mora causada,
     * igual que en el procesamiento de pago.
     */
    private void updateQuotaStatusAfterReversal(Long quotaId) {
        CreditAmortizationNEntity cuota = amortizationRepository.findById(quotaId).orElse(null);
        if (cuota == null) return;

        QuotaComponentsDto comp = resolveQuotaComponents(cuota);

        if (comp.totalPaid().compareTo(comp.totalDebt()) < 0) {
            cuota.setPaidFull("N");
        }
        if (comp.getInterestPaid().compareTo(BigDecimal.ZERO) <= 0) {
            cuota.setLiquidated("N");
        }
        amortizationRepository.save(cuota);
    }

    private NewRecaudoEntity buildRecaudoHeader(
            RecaudoRequestDto dto,
            MultipartFile file,
            Long quotaId,
            BigDecimal valuePaid) throws Exception {

        return NewRecaudoEntity.builder()
                .creditId(dto.getCreditId())
                .quotaId(quotaId)
                .valuePaid(valuePaid.negate())
                .paymentTypeId(dto.getPaymentTypeId())
                .bankId(dto.getBankId())
                .accountNumber(dto.getAccountNumber())
                .fileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .size(file != null ? file.getSize() : null)
                .fileData(file != null ? file.getBytes() : null)
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void saveDetail(Long recaudoId, Long typeRecaudoId, Long conceptId, BigDecimal value) {
        recaudoDetailRepository.save(RecaudoDetailEntity.builder()
                .recaudoId(recaudoId)
                .typeRecaudoId(typeRecaudoId)
                .conceptId(conceptId)
                .value(value)
                .build());
    }

    private void saveOtherConceptsDetail(Long otherConceptId, Long conceptId, BigDecimal value) {
        otherConceptDetailRepository.save(CreditOtherConceptDetailEntity.builder()
                .creditOtherConceptId(otherConceptId)
                .conceptId(conceptId)
                .value(value)
                .build());
    }

    private void saveDetailIfNonZero(
            Long recaudoId, Long typeRecaudoId, Long conceptId, BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
            saveDetail(recaudoId, typeRecaudoId, conceptId, value);
        }
    }

    private BigDecimal sumDetailByGlotypeAbs(List<RecaudoDetailEntity> details, Long conceptId) {
        return details.stream()
                .filter(d -> Objects.equals(d.getConceptId(), conceptId))
                .map(RecaudoDetailEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();
    }

    private BigDecimal sumAmortDetailByConcept(
            List<CreditAmortizationDetailEntity> details, Long conceptId) {
        return details.stream()
                .filter(d -> Objects.equals(d.getConceptId().longValue(), conceptId))
                .map(CreditAmortizationDetailEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long resolveGlotypeId(String gloCode) {
        return glotypesRepository.findByKeyAndCode(KEY_TIPCON, gloCode)
                .map(GlotypesEntity::getId)
                .orElseThrow(() -> new RuntimeException("Glotype no encontrado: " + gloCode));
    }

    private void validateClosingStatus(
            RecaudoRequestDto requestDto, Long personId, String token) {

        List<ClosingResponseDto> closings = closingAdapter.getByPersonId(personId, token);
        boolean hasActiveClosing = closings.stream()
                .anyMatch(c -> "PRE_CIERRE".equalsIgnoreCase(c.getClosingStatus()));

        if (!hasActiveClosing) {
            throw new BadRequestException("No puede recaudar sin un cierre activo");
        }
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    // ── Clase interna de distribución ─────────────────────────────────────────
    @lombok.Data
    private static class PaymentDistribution {
        private BigDecimal portfolioInsuranceApplied = BigDecimal.ZERO;
        private BigDecimal lifeInsuranceApplied      = BigDecimal.ZERO;
        private BigDecimal interestApplied           = BigDecimal.ZERO;
        private BigDecimal investmentApplied         = BigDecimal.ZERO;
        private BigDecimal moraApplied               = BigDecimal.ZERO;
        private BigDecimal totalApplied              = BigDecimal.ZERO;
    }

    @lombok.Builder
    private static class OptimizationContext {
        Map<String, Long> glotypes;
        Map<Long, List<CreditAmortizationDetailEntity>> amorDetailsByQuota;
        Map<Long, List<RecaudoDetailEntity>> recDetailsByQuota;
        Map<Long, List<RecaudoDetailEntity>> recDetailsByRecaudo;
        Map<Integer, List<CreditOtherConceptDetailEntity>> otherDetailsByQuotaNumber;
        Map<Long, Integer> quotaNumberByQuotaId;
    }
}