package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.rest_api.RecaudoRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.ReverseCapitalInterestRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.ReverseRecaudoRequestDto;
import com.recaudo.api.domain.model.entity.AmortizationEntity;
import com.recaudo.api.domain.model.entity.ConceptEntity;
import com.recaudo.api.domain.model.entity.CreditEntity;
import com.recaudo.api.domain.model.entity.PeriodEntity;
import com.recaudo.api.domain.model.entity.RecaudoEntity;
import com.recaudo.api.infrastructure.repository.AmortizationRepository;
import com.recaudo.api.infrastructure.repository.ConceptRepository;
import com.recaudo.api.infrastructure.repository.CreditRepository;
import com.recaudo.api.infrastructure.repository.PeriodRepository;
import com.recaudo.api.infrastructure.repository.RecaudoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecaudoAdapter {

    @Autowired
    private AmortizationRepository amortizationRepository;

    @Autowired
    private RecaudoRepository recaudoRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private CollectionVisitAdapter collectionVisitAdapter;



    /**
     * Obtiene el estado completo de pago de un crédito
     */
    @Transactional(readOnly = true)
    public CreditRecaudoStatusDto getCreditPaymentStatus(Long creditId) {
        try {

            // Obtener información del crédito
            CreditEntity credit = creditRepository.findById(creditId)
                    .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

            Optional<PeriodEntity> periodOpt =
                    periodRepository.findById(credit.getPeriodId());

            String periodName;
            if (periodOpt.isPresent()) {
                periodName = periodOpt.get().getName();
            } else {
                periodName = "N/A";
            }


            ConceptEntity conceptRecaudo = conceptRepository.findByConceptKey("RR")
                    .orElseThrow(() -> new RuntimeException("No se encontró el concepto de recaudo"));

            // Obtener todas las cuotas del crédito
            List<AmortizationEntity> cuotas = amortizationRepository
                    .findByCreditIdOrderByQuotaNumberAsc(creditId);

            if (cuotas.isEmpty()) {
                throw new RuntimeException("No se encontraron cuotas para este crédito");
            }

            // Obtener todos los recaudos del crédito
            List<RecaudoEntity> recaudos = recaudoRepository
                    .findRecaudosRRByCreditId(creditId);

            //Calcular resumen de cuotas
            long cuotasPagadas = cuotas.stream()
                    .filter(c -> "S".equals(c.getPaidFull()))
                    .count();

            long cuotasPendientes = cuotas.stream()
                    .filter(c -> "N".equals(c.getPaidFull()))
                    .count();

            // Calcular totales financieros usando BigDecimal
            BigDecimal totalPagado = recaudos.stream()
                    .filter(r -> r.getValuePaid().compareTo(BigDecimal.ZERO) < 0) //AJUSTAR
                    .map(r -> r.getValuePaid().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPendiente = cuotas.stream()
                    .filter(c -> "N".equals(c.getPaidFull()))
                    .map(c ->
                            c.getPortfolioInsurance()
                                    .add(c.getLifeInsurance())
                                    .add(c.getInterestValue())
                                    .add(c.getInvestmentValue())
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredito = credit.getTotalIntentionValue();
            BigDecimal porcentajePagado = BigDecimal.ZERO;
            if (totalCredito.compareTo(BigDecimal.ZERO) > 0) {
                porcentajePagado = totalPagado
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalCredito, 2, RoundingMode.HALF_UP);
            }

            // Obtener tasa de interés del crédito
            BigDecimal tasaCredito = credit.getTaxValue()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            // Obtener todos los conceptos usados en los recaudos
            Set<Long> conceptIds = recaudos.stream()
                    .map(RecaudoEntity::getConceptId)
                    .collect(Collectors.toSet());

            Map<Long, ConceptEntity> conceptMap = conceptRepository
                    .findAllById(conceptIds)
                    .stream()
                    .collect(Collectors.toMap(ConceptEntity::getId, c -> c));

            // Mapear detalles de cuotas
            LocalDate today = LocalDate.now();
            List<QuotaDetailDto> cuotasDetail = cuotas.stream()
                    .map(cuota -> {

                        // Calcular lo ya pagado de esta cuota
                        BigDecimal totalPagadoCuota = recaudoRepository.getTotalByCuotaId(cuota.getId());

                        // Calcular saldo pendiente REAL
                        BigDecimal saldoPendiente = cuota.getQuotaValue().subtract(totalPagadoCuota);

                        // Asegurar que no sea negativo
                        if (saldoPendiente.compareTo(BigDecimal.ZERO) < 0) {
                            saldoPendiente = BigDecimal.ZERO;
                        }

                        BigDecimal totalPend = cuota.getPortfolioInsurance()
                                .add(cuota.getLifeInsurance())
                                .add(cuota.getInterestValue())
                                .add(cuota.getInvestmentValue());

                        // Calcular intereses moratorios
                        BigDecimal delayPenalty = BigDecimal.ZERO;
                        int daysOverdue = 0;

                        if (cuota.getExpirationDate().isBefore(today) && "N".equals(cuota.getPaidFull())) {
                            // Calcular días de mora
                            daysOverdue = (int) ChronoUnit.DAYS.between(cuota.getExpirationDate(), today);

                            // Calcular períodos vencidos = días de mora / 30
                            BigDecimal periodosVencidos = BigDecimal.valueOf(daysOverdue)
                                    .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);

                            // Calcular interés moratorio = saldoPendiente * tasa * períodos vencidos
                            delayPenalty = saldoPendiente
                                    .multiply(tasaCredito)
                                    .multiply(periodosVencidos)
                                    .setScale(2, RoundingMode.HALF_UP);
                        }

                        return QuotaDetailDto.builder()
                                .quotaId(cuota.getId())
                                .quotaNumber(cuota.getQuotaNumber())
                                .expirationDate(String.valueOf(cuota.getExpirationDate()))
                                .liquidated(cuota.getLiquidated())
                                .paidFull(cuota.getPaidFull())
                                .quotaValue(cuota.getQuotaValue())
                                .totalPaid(totalPagadoCuota)
                                .remainingBalance(saldoPendiente)
                                .portfolioInsurancePending(cuota.getPortfolioInsurance())
                                .lifeInsurancePending(cuota.getLifeInsurance())
                                .interestPending(cuota.getInterestValue())
                                .investmentPending(cuota.getInvestmentValue())
                                .totalPending(totalPend)
                                .isPaid("S".equals(cuota.getPaidFull()))
                                .delayPenalty(delayPenalty)
                                .daysOverdue(daysOverdue)
                                .hasInterestPayment("S".equals(cuota.getLiquidated()))

                                .isOverdue(cuota.getExpirationDate().isBefore(today) &&
                                "N".equals(cuota.getPaidFull()))
                                .build();
                    })
                    .collect(Collectors.toList());


            // Mapear detalles de recaudos
            List<RecaudoDetailDto> recaudosDetail = recaudos.stream()
                    .map(recaudo -> {

                        Integer quotaNumber = cuotas.stream()
                                .filter(c -> Objects.equals(c.getId(), recaudo.getCuotaId()))
                                .findFirst()
                                .map(AmortizationEntity::getQuotaNumber)
                                .orElse(null);


                        ConceptEntity concept = conceptMap.get(recaudo.getConceptId());

                        return RecaudoDetailDto.builder()
                                .recaudoId(recaudo.getId())
                                .quotaNumber(quotaNumber)
                                .conceptName(concept != null ? concept.getName() : null)
                                .valuePaid(
                                        recaudo.getValuePaid() != null
                                                ? recaudo.getValuePaid()
                                                : BigDecimal.ZERO
                                )
                                .investmentValue(
                                        recaudo.getInvestmentValue() != null
                                                ? recaudo.getInvestmentValue()
                                                : BigDecimal.ZERO
                                )
                                .interestValue(
                                        recaudo.getInterestValue() != null
                                                ? recaudo.getInterestValue()
                                                : BigDecimal.ZERO
                                )
                                .lifeInsurance(
                                        recaudo.getLifeInsurance() != null
                                                ? recaudo.getLifeInsurance()
                                                : BigDecimal.ZERO
                                )
                                .portfolioInsurance(
                                        recaudo.getPortfolioInsurance() != null
                                                ? recaudo.getPortfolioInsurance()
                                                : BigDecimal.ZERO
                                )
                                .delayPenalty(
                                        recaudo.getDelayPenalty() != null
                                                ? recaudo.getDelayPenalty()
                                                : BigDecimal.ZERO
                                )
                                .userCreate(recaudo.getUserCreate())
                                .createdAt(String.valueOf(recaudo.getCreatedAt()))
                                .build();
                    })
                    .collect(Collectors.toList());


            BigDecimal realQuotaValue = cuotas.isEmpty() ?
                    BigDecimal.ZERO :
                    cuotas.get(0).getQuotaValue();

            // Construir respuesta completa
            CreditRecaudoStatusDto status = CreditRecaudoStatusDto.builder()
                    .creditId(creditId)
                    .personId(credit.getPersonId())
                    .quotaValue(realQuotaValue)
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

            log.info("Estado de pago obtenido exitosamente: cuotasPagadas={}, cuotasPendientes={}, porcentajePagado={}%",
                    cuotasPagadas, cuotasPendientes, status.getPorcentajePagado());

            return status;

        } catch (Exception e) {
            log.error("Error al consultar estado de pago del crédito ID: {}", creditId, e);
            throw new RuntimeException("Error al consultar estado de pago: " + e.getMessage(), e);
        }
    }

    public List<RecaudoResponseProjection> getRecaudosByUserAndDate(
            Long closinId,
            LocalDate fecha,
            Long zonaId
    ) {
        log.info("Consultando recaudos del asesor {} para la fecha {}", closinId, fecha);

        List<RecaudoResponseProjection> recaudos =
                recaudoRepository.findRecaudosWithClientName(closinId, fecha, zonaId);

        if (recaudos.isEmpty()) {
            log.warn("No se encontraron recaudos para el asesor {} en la fecha {}", closinId, fecha);
        }

        return recaudos;
    }

    public List<CreditIntentionResponseProjection> getIntentionsByUserAndDate(
            Long closingId,
            LocalDate fecha,
            Long zonaId
    ) {
        log.info("Consultando intenciones de crédito del asesor en closing {} para la fecha {} y zona {}",
                closingId, fecha, zonaId);

        List<CreditIntentionResponseProjection> intentions =
                recaudoRepository.findIntentionsByUserAndDate(closingId, fecha, zonaId);

        if (intentions.isEmpty()) {
            log.warn("No se encontraron intenciones de crédito para el asesor en closing {} en la fecha {} y zona {}",
                    closingId, fecha, zonaId);
        }

        return intentions;
    }

    @Transactional
    public RecaudoResultDto processPayment(RecaudoRequestDto requestDto, MultipartFile file) {
        try {
            // Validar que el valor pagado sea positivo
            if (requestDto.getValuePaid().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El valor pagado debe ser mayor a cero");
            }

            // Validar tipo de distribución
            String distributionType = requestDto.getDistributionType() != null
                    ? requestDto.getDistributionType()
                    : "NORMAL";

            if (!distributionType.equals("RECAUDO_RUTA")
                    &&!distributionType.equals("NORMAL")
                    && !distributionType.equals("RECAUDO_CAPITAL")
                    && !distributionType.equals("RECAUDO_INTERESES")
                    && !distributionType.equals("AJUSTE_PERDIDA")) {
                throw new IllegalArgumentException("Tipo de distribución inválido: " + distributionType);
            }

            // LÓGICA DIVIDIDA: tipos que NO recorren vs tipos que SÍ recorren
            if (distributionType.equals("RECAUDO_CAPITAL") || distributionType.equals("RECAUDO_INTERESES")) {
                return processSingleQuotaPayment(requestDto, file, distributionType);
            } else {
                return processMultipleQuotasPayment(requestDto, file, distributionType);
            }

        } catch (Exception e) {
            log.error("Error al procesar el pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el pago: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa pagos que afectan UNA SOLA CUOTA sin recorrer
     * (RECAUDO_CAPITAL, RECAUDO_INTERESES)
     */
    private RecaudoResultDto processSingleQuotaPayment(
            RecaudoRequestDto requestDto,
            MultipartFile file,
            String distributionType
    ) throws Exception {


        // Siempre usar NC
        ConceptEntity conceptRecaudo = conceptRepository.findByConceptKey("NC")
                .orElseThrow(() -> new RuntimeException("No se encontró el concepto NC"));

        BigDecimal investmentApplied = BigDecimal.ZERO;
        BigDecimal interestApplied = BigDecimal.ZERO;

        if (distributionType.equals("RECAUDO_CAPITAL")) {
            investmentApplied = requestDto.getValuePaid();
        } else { // RECAUDO_INTERESES
            interestApplied = requestDto.getValuePaid();
        }

        // Registro unico en recaudo
        RecaudoEntity recaudo = RecaudoEntity.builder()
                .creditId(requestDto.getCreditId())
                .conceptId(conceptRecaudo.getId())
                .valuePaid(BigDecimal.ZERO)
                .paymentTypeId(requestDto.getPaymentTypeId())
                .bankId(requestDto.getBankId())
                .accountNumber(requestDto.getAccountNumber())
                .fileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .size(file != null ? file.getSize() : null)
                .fileData(file != null ? file.getBytes() : null)
                .portfolioInsurance(BigDecimal.ZERO)
                .lifeInsurance(BigDecimal.ZERO)
                .interestValue(interestApplied.compareTo(BigDecimal.ZERO) > 0
                        ? interestApplied.negate()
                        : BigDecimal.ZERO)
                .investmentValue(investmentApplied.compareTo(BigDecimal.ZERO) > 0
                        ? investmentApplied.negate()
                        : BigDecimal.ZERO)
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();

        recaudoRepository.save(recaudo);

        return RecaudoResultDto.builder()
                .creditId(requestDto.getCreditId())
                .totalPaid(requestDto.getValuePaid())
                .cuotasPagadas(0)
                .cuotasFaltantes(0)
                .saldoSobrante(BigDecimal.ZERO)
                .build();
    }

    /**
     * Procesa pagos que RECORRE AMORTIZACION
     * (NORMAL, RECAUDO_EN_RUTA, AJUSTE_PERDIDA)
     */
    private RecaudoResultDto processMultipleQuotasPayment(
            RecaudoRequestDto requestDto,
            MultipartFile file,
            String distributionType
    ) throws Exception {

        // Determinar el concepto según el tipo
        String conceptKey;
        switch (distributionType) {
            case "AJUSTE_PERDIDA":
                conceptKey = "AC"; // AJUSTE CREDITO
                break;
            case "NORMAL":
                conceptKey = "NC"; // NOTA CREDITO
                break;

            case "RECAUDO_RUTA":
                conceptKey = "RR"; // RECAUDO EN RUTA
                break;
            default: // RECAUDO_EN_RUTA u otros
                conceptKey = "RR"; // RECAUDO EN RUTA
                break;
        }

        ConceptEntity conceptRecaudo = conceptRepository.findByConceptKey(conceptKey)
                .orElseThrow(() -> new RuntimeException("No se encontró el concepto: " + conceptKey));

        // Obtener todas las cuotas pendientes del crédito ordenadas por número de cuota
        List<AmortizationEntity> cuotasPendientes = amortizationRepository
                .findByCreditIdAndPaidFullOrderByQuotaNumberAsc(requestDto.getCreditId(), "N");

        if (cuotasPendientes.isEmpty()) {
            throw new RuntimeException("No hay cuotas pendientes para este crédito");
        }

        // Variables para el resultado
        BigDecimal saldoRestante = requestDto.getValuePaid();
        int cuotasLiquidadas = 0;

        // Recorrer cada cuota pendiente
        for (AmortizationEntity cuota : cuotasPendientes) {
            if (saldoRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            collectionVisitAdapter.registerDailyVisitIfNotExists(
                    requestDto.getCreditId(),
                    cuota.getId(),
                    getUsernameToken(),
                    LocalDate.now()
            );

            // Calcular cuánto ya se ha pagado de esta cuota
            BigDecimal totalPagadoCuota = recaudoRepository.getTotalByCuotaId(cuota.getId()).abs();
            BigDecimal pendienteCuota = cuota.getQuotaValue().subtract(totalPagadoCuota);

            if (pendienteCuota.compareTo(BigDecimal.ZERO) <= 0) {
                cuota.setLiquidated("S");
                cuota.setPaidFull("S");
                amortizationRepository.save(cuota);
                collectionVisitAdapter.markAsPaidToday(cuota.getId(), LocalDate.now());
                continue;
            }

            // Distribuir el pago según prioridad (NORMAL para todos estos tipos)
            PaymentDistribution distribution = distributePaymentNormal(cuota, saldoRestante);

            // Crear registro de recaudo
            RecaudoEntity recaudo = RecaudoEntity.builder()
                    .creditId(requestDto.getCreditId())
                    .cuotaId(cuota.getId())
                    .conceptId(conceptRecaudo.getId())
                    .valuePaid(distribution.getTotalApplied().negate())
                    .paymentTypeId(requestDto.getPaymentTypeId())
                    .bankId(requestDto.getBankId())
                    .accountNumber(requestDto.getAccountNumber())
                    .fileName(file != null ? file.getOriginalFilename() : null)
                    .contentType(file != null ? file.getContentType() : null)
                    .size(file != null ? file.getSize() : null)
                    .fileData(file != null ? file.getBytes() : null)
                    .portfolioInsurance(distribution.getPortfolioInsuranceApplied().negate())
                    .lifeInsurance(distribution.getLifeInsuranceApplied().negate())
                    .interestValue(distribution.getInterestApplied().negate())
                    .investmentValue(distribution.getInvestmentApplied().negate())
                    .userCreate(getUsernameToken())
                    .createdAt(LocalDateTime.now())
                    .build();

            recaudoRepository.save(recaudo);

            // Recalcular estado de la cuota
            BigDecimal nuevoTotalPagado = totalPagadoCuota.add(distribution.getTotalApplied());

            // Marcar liquidated si recibió algún pago a intereses
            if (distribution.getInterestApplied().compareTo(BigDecimal.ZERO) > 0) {
                cuota.setLiquidated("S");
            }

            // Verificar si la cuota quedó completamente liquidada
            if (nuevoTotalPagado.compareTo(cuota.getQuotaValue()) >= 0) {
                cuota.setPaidFull("S");
                cuotasLiquidadas++;
                collectionVisitAdapter.markAsPaidToday(cuota.getId(), LocalDate.now());
            }

            amortizationRepository.save(cuota);

            // Actualizar saldo restante
            saldoRestante = saldoRestante.subtract(distribution.getTotalApplied());
        }

        return RecaudoResultDto.builder()
                .creditId(requestDto.getCreditId())
                .totalPaid(requestDto.getValuePaid())
                .cuotasPagadas(cuotasLiquidadas)
                .cuotasFaltantes(cuotasPendientes.size() - cuotasLiquidadas)
                .saldoSobrante(saldoRestante.max(BigDecimal.ZERO))
                .build();
    }

    /*@Transactional
    public RecaudoResultDto processPayment(RecaudoRequestDto requestDto, MultipartFile file) {
        try {

            // Validar que el valor pagado sea positivo
            if (requestDto.getValuePaid().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El valor pagado debe ser mayor a cero");
            }

            // Validar tipo de distribución
            String distributionType = requestDto.getDistributionType() != null
                    ? requestDto.getDistributionType()
                    : "NORMAL";

            if (!distributionType.equals("NORMAL") &&
                    !distributionType.equals("SOLO_CAPITAL") &&
                    !distributionType.equals("SOLO_INTERESES")) {
                throw new IllegalArgumentException("Tipo de distribución inválido: " + distributionType);
            }

            // Obtener el concepto (puede ser RR o NC según el caso)
            String conceptKey = distributionType.equals("NORMAL") ? "RR" : "NC";
            ConceptEntity conceptRecaudo = conceptRepository.findByConceptKey(conceptKey)
                    .orElseThrow(() -> new RuntimeException("No se encontró el concepto: " + conceptKey));

            // Obtener todas las cuotas pendientes del crédito ordenadas por número de cuota
            List<AmortizationEntity> cuotasPendientes = amortizationRepository
                    .findByCreditIdAndPaidFullOrderByQuotaNumberAsc(requestDto.getCreditId(), "N");

            if (cuotasPendientes.isEmpty()) {
                throw new RuntimeException("No hay cuotas pendientes para este crédito");
            }

            // Variables para el resultado
            BigDecimal saldoRestante = requestDto.getValuePaid();
            int cuotasLiquidadas = 0;

            // Recorrer cada cuota pendiente
            for (AmortizationEntity cuota : cuotasPendientes) {
                if (saldoRestante.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                collectionVisitAdapter.registerDailyVisitIfNotExists(
                        requestDto.getCreditId(),
                        cuota.getId(),
                        getUsernameToken(),
                        LocalDate.now()
                );

                // Calcular cuánto ya se ha pagado de esta cuota
                BigDecimal totalPagadoCuota = recaudoRepository
                        .getTotalByCuotaId(cuota.getId());

                BigDecimal pendienteCuota = cuota.getQuotaValue().subtract(totalPagadoCuota);

                if (pendienteCuota.compareTo(BigDecimal.ZERO) <= 0) {
                    cuota.setLiquidated("S");
                    cuota.setPaidFull("S");
                    amortizationRepository.save(cuota);
                    collectionVisitAdapter.markAsPaidToday(cuota.getId(), LocalDate.now());
                    continue;
                }

                // SELECCIONAR TIPO DE DISTRIBUCIÓN
                PaymentDistribution distribution;

                switch (distributionType) {
                    case "SOLO_CAPITAL":
                        distribution = distributePaymentCapitalOnly(cuota, saldoRestante);
                        break;

                    case "SOLO_INTERESES":
                        distribution = distributePaymentInterestOnly(cuota, saldoRestante);
                        break;

                    default: // "NORMAL"
                        distribution = distributePaymentNormal(cuota, saldoRestante);
                        break;
                }

                // Crear registro de recaudo
                RecaudoEntity recaudo = RecaudoEntity.builder()
                        .creditId(requestDto.getCreditId())
                        .cuotaId(cuota.getId())
                        .conceptId(conceptRecaudo.getId())
                        .valuePaid(distribution.getTotalApplied().negate()) // Negativo porque es recaudo
                        .paymentTypeId(requestDto.getPaymentTypeId())
                        .bankId(requestDto.getBankId())
                        .accountNumber(requestDto.getAccountNumber())
                        .fileName(file != null ? file.getOriginalFilename() : null)
                        .contentType(file != null ? file.getContentType() : null)
                        .size(file != null ? file.getSize() : null)
                        .fileData(file != null ? file.getBytes() : null)
                        .portfolioInsurance(distribution.getPortfolioInsuranceApplied().negate())
                        .lifeInsurance(distribution.getLifeInsuranceApplied().negate())
                        .interestValue(distribution.getInterestApplied().negate())
                        .investmentValue(distribution.getInvestmentApplied().negate())
                        .userCreate(getUsernameToken())
                        .createdAt(LocalDateTime.now())
                        .build();

                recaudoRepository.save(recaudo);

                // Recalcular estado de la cuota
                BigDecimal nuevoTotalPagado = totalPagadoCuota
                        .add(distribution.getTotalApplied());

                // LÓGICA PARA MARCAR LIQUIDATED
                boolean debeMarcarLiquidated = false;

                if (distributionType.equals("SOLO_INTERESES")) {
                    // Para SOLO_INTERESES: marcar si cubre TODOS los intereses
                    BigDecimal interestPaid = recaudoRepository.getInterestByCuotaId(cuota.getId());
                    BigDecimal totalInterestPaid = interestPaid.add(distribution.getInterestApplied());

                    if (totalInterestPaid.compareTo(cuota.getInterestValue()) >= 0) {
                        debeMarcarLiquidated = true;
                    }
                } else {
                    // Para NORMAL y SOLO_CAPITAL: marcar si recibió algún pago a intereses
                    if (distribution.getInterestApplied().compareTo(BigDecimal.ZERO) > 0) {
                        debeMarcarLiquidated = true;
                    }
                }

                if (debeMarcarLiquidated) {
                    cuota.setLiquidated("S");
                }

                // Verificar si la cuota quedó completamente liquidada
                if (nuevoTotalPagado.compareTo(cuota.getQuotaValue()) >= 0) {
                    cuota.setPaidFull("S");
                    cuotasLiquidadas++;
                    collectionVisitAdapter.markAsPaidToday(cuota.getId(), LocalDate.now());
                }

                amortizationRepository.save(cuota);

                // Actualizar saldo restante
                saldoRestante = saldoRestante.subtract(distribution.getTotalApplied());
            }

            return RecaudoResultDto.builder()
                    .creditId(requestDto.getCreditId())
                    .totalPaid(requestDto.getValuePaid())
                    .cuotasPagadas(cuotasLiquidadas)
                    .cuotasFaltantes(cuotasPendientes.size() - cuotasLiquidadas)
                    .saldoSobrante(saldoRestante.max(BigDecimal.ZERO))
                    .build();

        } catch (Exception e) {
            log.error("Error al procesar el pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el pago: " + e.getMessage(), e);
        }
    }*/


    /**
     * Distribución NORMAL, Distribuye el pago según el orden de prioridad
     * RECAUDO NORMAL, RECAUDO EN RUTA y AJUSTE POR PERDIDA
     */
    private PaymentDistribution distributePaymentNormal(AmortizationEntity cuota, BigDecimal availableAmount) {
        PaymentDistribution distribution = new PaymentDistribution();
        BigDecimal remaining = availableAmount;

        // Calcular cuánto ya se ha pagado de cada concepto
        BigDecimal portfolioInsurancePaid = recaudoRepository.getPortfolioInsuranceByCuotaId(cuota.getId()).abs();
        BigDecimal lifeInsurancePaid = recaudoRepository.getLifeInsuranceByCuotaId(cuota.getId()).abs();
        BigDecimal interestPaid = recaudoRepository.getInterestByCuotaId(cuota.getId()).abs();
        BigDecimal investmentPaid = recaudoRepository.getInvestmentByCuotaId(cuota.getId()).abs();


        // 1. Aplicar a Seguro de Cartera
        BigDecimal portfolioInsurancePending = cuota.getPortfolioInsurance().subtract(portfolioInsurancePaid);
        BigDecimal portfolioInsuranceApplied = remaining.min(portfolioInsurancePending);
        distribution.setPortfolioInsuranceApplied(portfolioInsuranceApplied);
        remaining = remaining.subtract(portfolioInsuranceApplied);

        // 2. Aplicar a Seguro de Vida
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lifeInsurancePending = cuota.getLifeInsurance().subtract(lifeInsurancePaid);
            BigDecimal lifeInsuranceApplied = remaining.min(lifeInsurancePending);
            distribution.setLifeInsuranceApplied(lifeInsuranceApplied);
            remaining = remaining.subtract(lifeInsuranceApplied);
        }

        // 3. Aplicar a Intereses
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal interestPending = cuota.getInterestValue().subtract(interestPaid);
            BigDecimal interestApplied = remaining.min(interestPending);
            distribution.setInterestApplied(interestApplied);
            remaining = remaining.subtract(interestApplied);
        }

        // 4. Aplicar a Capital/Inversión
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal investmentPending = cuota.getInvestmentValue().subtract(investmentPaid);
            BigDecimal investmentApplied = remaining.min(investmentPending);
            distribution.setInvestmentApplied(investmentApplied);
            remaining = remaining.subtract(investmentApplied);
        }

        distribution.setTotalApplied(availableAmount.subtract(remaining));
        distribution.setDistributionType("NORMAL");

        return distribution;
    }


    //Distribución SOLO CAPITAL
    private PaymentDistribution distributePaymentCapitalOnly(AmortizationEntity cuota, BigDecimal availableAmount) {
        PaymentDistribution distribution = new PaymentDistribution();

        // Calcular cuánto ya se ha pagado de capital
        BigDecimal investmentPaid = recaudoRepository.getInvestmentByCuotaId(cuota.getId());

        // Calcular pendiente de capital
        BigDecimal investmentPending = cuota.getInvestmentValue().subtract(investmentPaid);

        // Aplicar TODO el pago disponible solo a capital (hasta el límite pendiente)
        BigDecimal investmentApplied = availableAmount.min(investmentPending);

        distribution.setInvestmentApplied(investmentApplied);
        distribution.setPortfolioInsuranceApplied(BigDecimal.ZERO);
        distribution.setLifeInsuranceApplied(BigDecimal.ZERO);
        distribution.setInterestApplied(BigDecimal.ZERO);
        distribution.setTotalApplied(investmentApplied);
        distribution.setDistributionType("SOLO_CAPITAL");

        return distribution;
    }


    //Distribución SOLO INTERESES
    private PaymentDistribution distributePaymentInterestOnly(AmortizationEntity cuota, BigDecimal availableAmount) {
        PaymentDistribution distribution = new PaymentDistribution();

        // Calcular cuánto ya se ha pagado de intereses
        BigDecimal interestPaid = recaudoRepository.getInterestByCuotaId(cuota.getId());

        // Calcular pendiente de intereses
        BigDecimal interestPending = cuota.getInterestValue().subtract(interestPaid);

        // Aplicar TODO el pago disponible solo a intereses (hasta el límite pendiente)
        BigDecimal interestApplied = availableAmount.min(interestPending);

        distribution.setInterestApplied(interestApplied);
        distribution.setPortfolioInsuranceApplied(BigDecimal.ZERO);
        distribution.setLifeInsuranceApplied(BigDecimal.ZERO);
        distribution.setInvestmentApplied(BigDecimal.ZERO);
        distribution.setTotalApplied(interestApplied);
        distribution.setDistributionType("SOLO_INTERESES");
        return distribution;
    }

    /**
     * REVERSAR RECAUDO COMPLETO
     * Revierte uno o más recaudos completos de cuotas específicas
     */
    @Transactional
    public RecaudoResultDto reverseRecaudos(ReverseRecaudoRequestDto requestDto) {
        try {
            if (requestDto.getRecaudoIds() == null || requestDto.getRecaudoIds().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un recaudo a reversar");
            }

            ConceptEntity conceptND = conceptRepository.findByConceptKey("ND")
                    .orElseThrow(() -> new RuntimeException("No se encontró el concepto ND (Nota Débito)"));

            BigDecimal totalReversado = BigDecimal.ZERO;
            int recaudosReversados = 0;

            for (Long recaudoId : requestDto.getRecaudoIds()) {
                RecaudoEntity recaudoOriginal = recaudoRepository.findById(recaudoId)
                        .orElseThrow(() -> new RuntimeException("Recaudo no encontrado: " + recaudoId));

                // Validar que pertenezca al crédito
                if (!recaudoOriginal.getCreditId().equals(requestDto.getCreditId())) {
                    throw new IllegalArgumentException("El recaudo " + recaudoId + " no pertenece al crédito especificado");
                }

                // Crear registro de reversión (valores positivos, opuestos al recaudo original)
                RecaudoEntity reversa = RecaudoEntity.builder()
                        .creditId(recaudoOriginal.getCreditId())
                        .cuotaId(recaudoOriginal.getCuotaId())
                        .conceptId(conceptND.getId())
                        .valuePaid(recaudoOriginal.getValuePaid().negate())
                        .paymentTypeId(recaudoOriginal.getPaymentTypeId())
                        .bankId(recaudoOriginal.getBankId())
                        .accountNumber(recaudoOriginal.getAccountNumber())
                        .portfolioInsurance(recaudoOriginal.getPortfolioInsurance().negate())
                        .lifeInsurance(recaudoOriginal.getLifeInsurance().negate())
                        .interestValue(recaudoOriginal.getInterestValue().negate())
                        .investmentValue(recaudoOriginal.getInvestmentValue().negate())
                        .userCreate(getUsernameToken())
                        .createdAt(LocalDateTime.now())
                        .build();

                recaudoRepository.save(reversa);

                /* Actualizar estado de la cuota
                if (recaudoOriginal.getCuotaId() != null) {
                    AmortizationEntity cuota = amortizationRepository.findById(recaudoOriginal.getCuotaId())
                            .orElse(null);

                    if (cuota != null) {
                        // Recalcular totales
                        BigDecimal totalPagadoCuota = recaudoRepository.getTotalByCuotaId(cuota.getId());

                        // Actualizar estado
                        if (totalPagadoCuota.compareTo(cuota.getQuotaValue()) < 0) {
                            cuota.setPaidFull("N");
                        }

                        // Verificar si aún tiene pago de intereses
                        BigDecimal interestPaid = recaudoRepository.getInterestByCuotaId(cuota.getId());
                        if (interestPaid.compareTo(BigDecimal.ZERO) <= 0) {
                            cuota.setLiquidated("N");
                        }

                        amortizationRepository.save(cuota);
                    }
                }
                 */

                totalReversado = totalReversado.add(recaudoOriginal.getValuePaid().abs());
                recaudosReversados++;
            }

            log.info("Reversión completada: {} recaudos reversados, total: {}",
                    recaudosReversados, totalReversado);

            return RecaudoResultDto.builder()
                    .creditId(requestDto.getCreditId())
                    .totalPaid(totalReversado)
                    .cuotasPagadas(0)
                    .cuotasFaltantes(0)
                    .saldoSobrante(BigDecimal.ZERO)
                    .build();

        } catch (Exception e) {
            log.error("Error al reversar recaudos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reversar recaudos: " + e.getMessage(), e);
        }
    }

    /**
     * REVERSAR SOLO CAPITAL
     * Revierte pagos específicos a capital
     */
    @Transactional
    public RecaudoResultDto reverseCapital(ReverseCapitalInterestRequestDto requestDto) {
        try {
            if (requestDto.getRecaudoIds() == null || requestDto.getRecaudoIds().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un pago de capital a reversar");
            }

            ConceptEntity conceptND = conceptRepository.findByConceptKey("ND")
                    .orElseThrow(() -> new RuntimeException("No se encontró el concepto ND"));

            BigDecimal totalReversado = BigDecimal.ZERO;

            for (Long recaudoId : requestDto.getRecaudoIds()) {
                RecaudoEntity recaudoOriginal = recaudoRepository.findById(recaudoId)
                        .orElseThrow(() -> new RuntimeException("Recaudo no encontrado: " + recaudoId));

                // Validar que pertenezca al crédito
                if (!recaudoOriginal.getCreditId().equals(requestDto.getCreditId())) {
                    throw new IllegalArgumentException("El recaudo no pertenece al crédito especificado");
                }

                // Solo reversar el componente de capital (investment_value)
                BigDecimal capitalARevertir = recaudoOriginal.getInvestmentValue().abs();

                if (capitalARevertir.compareTo(BigDecimal.ZERO) > 0) {
                    RecaudoEntity reversa = RecaudoEntity.builder()
                            .creditId(recaudoOriginal.getCreditId())
                            .cuotaId(recaudoOriginal.getCuotaId())
                            .conceptId(conceptND.getId())
                            .valuePaid(BigDecimal.ZERO)
                            .paymentTypeId(recaudoOriginal.getPaymentTypeId())
                            .bankId(recaudoOriginal.getBankId())
                            .accountNumber(recaudoOriginal.getAccountNumber())
                            .portfolioInsurance(BigDecimal.ZERO)
                            .lifeInsurance(BigDecimal.ZERO)
                            .interestValue(BigDecimal.ZERO)
                            .investmentValue(capitalARevertir) // Positivo (reversión)
                            .userCreate(getUsernameToken())
                            .createdAt(LocalDateTime.now())
                            .build();

                    recaudoRepository.save(reversa);

                    // Actualizar estado de cuota si aplica
                    if (recaudoOriginal.getCuotaId() != null) {
                        AmortizationEntity cuota = amortizationRepository.findById(recaudoOriginal.getCuotaId())
                                .orElse(null);

                        if (cuota != null) {
                            BigDecimal totalPagadoCuota = recaudoRepository.getTotalByCuotaId(cuota.getId());

                            if (totalPagadoCuota.compareTo(cuota.getQuotaValue()) < 0) {
                                cuota.setPaidFull("N");
                            }

                            amortizationRepository.save(cuota);
                        }
                    }

                    totalReversado = totalReversado.add(capitalARevertir);
                }
            }

            return RecaudoResultDto.builder()
                    .creditId(requestDto.getCreditId())
                    .totalPaid(totalReversado)
                    .cuotasPagadas(0)
                    .cuotasFaltantes(0)
                    .saldoSobrante(BigDecimal.ZERO)
                    .build();

        } catch (Exception e) {
            log.error("Error al reversar capital: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reversar capital: " + e.getMessage(), e);
        }
    }

    /**
     * REVERSAR SOLO INTERESES
     * Revierte pagos específicos a intereses
     */
    @Transactional
    public RecaudoResultDto reverseInterest(ReverseCapitalInterestRequestDto requestDto) {
        try {
            if (requestDto.getRecaudoIds() == null || requestDto.getRecaudoIds().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un pago de intereses a reversar");
            }

            ConceptEntity conceptND = conceptRepository.findByConceptKey("ND")
                    .orElseThrow(() -> new RuntimeException("No se encontró el concepto ND"));

            BigDecimal totalReversado = BigDecimal.ZERO;

            for (Long recaudoId : requestDto.getRecaudoIds()) {
                RecaudoEntity recaudoOriginal = recaudoRepository.findById(recaudoId)
                        .orElseThrow(() -> new RuntimeException("Recaudo no encontrado: " + recaudoId));

                // Validar que pertenezca al crédito
                if (!recaudoOriginal.getCreditId().equals(requestDto.getCreditId())) {
                    throw new IllegalArgumentException("El recaudo no pertenece al crédito especificado");
                }

                // Solo reversar el componente de intereses (interest_value)
                BigDecimal interesARevertir = recaudoOriginal.getInterestValue().abs();

                if (interesARevertir.compareTo(BigDecimal.ZERO) > 0) {
                    RecaudoEntity reversa = RecaudoEntity.builder()
                            .creditId(recaudoOriginal.getCreditId())
                            .cuotaId(recaudoOriginal.getCuotaId())
                            .conceptId(conceptND.getId())
                            .valuePaid(BigDecimal.ZERO)
                            .paymentTypeId(recaudoOriginal.getPaymentTypeId())
                            .bankId(recaudoOriginal.getBankId())
                            .accountNumber(recaudoOriginal.getAccountNumber())
                            .portfolioInsurance(BigDecimal.ZERO)
                            .lifeInsurance(BigDecimal.ZERO)
                            .interestValue(interesARevertir) // Positivo (reversión)
                            .investmentValue(BigDecimal.ZERO)
                            .userCreate(getUsernameToken())
                            .createdAt(LocalDateTime.now())
                            .build();

                    recaudoRepository.save(reversa);

                    // Actualizar estado de cuota si aplica
                    if (recaudoOriginal.getCuotaId() != null) {
                        AmortizationEntity cuota = amortizationRepository.findById(recaudoOriginal.getCuotaId())
                                .orElse(null);

                        if (cuota != null) {
                            BigDecimal totalPagadoCuota = recaudoRepository.getTotalByCuotaId(cuota.getId());

                            if (totalPagadoCuota.compareTo(cuota.getQuotaValue()) < 0) {
                                cuota.setPaidFull("N");
                            }

                            // Verificar si aún tiene pago de intereses
                            BigDecimal interestPaid = recaudoRepository.getInterestByCuotaId(cuota.getId());
                            if (interestPaid.compareTo(BigDecimal.ZERO) <= 0) {
                                cuota.setLiquidated("N");
                            }

                            amortizationRepository.save(cuota);
                        }
                    }

                    totalReversado = totalReversado.add(interesARevertir);
                }
            }

            return RecaudoResultDto.builder()
                    .creditId(requestDto.getCreditId())
                    .totalPaid(totalReversado)
                    .cuotasPagadas(0)
                    .cuotasFaltantes(0)
                    .saldoSobrante(BigDecimal.ZERO)
                    .build();

        } catch (Exception e) {
            log.error("Error al reversar intereses: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reversar intereses: " + e.getMessage(), e);
        }
    }

    //Clase interna para distribución de pagos
    @lombok.Data
    private static class PaymentDistribution {
        private BigDecimal portfolioInsuranceApplied = BigDecimal.ZERO;
        private BigDecimal lifeInsuranceApplied = BigDecimal.ZERO;
        private BigDecimal interestApplied = BigDecimal.ZERO;
        private BigDecimal investmentApplied = BigDecimal.ZERO;
        private BigDecimal totalApplied = BigDecimal.ZERO;
        private String distributionType;
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}