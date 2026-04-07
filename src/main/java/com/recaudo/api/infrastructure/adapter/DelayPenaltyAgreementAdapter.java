package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.DelayPenaltyAgreementGateway;
import com.recaudo.api.domain.model.dto.response.AgreementDetailResponseDto;
import com.recaudo.api.domain.model.dto.response.AgreementResponseDto;
import com.recaudo.api.domain.model.dto.response.PendingQuotaForAgreementDto;
import com.recaudo.api.domain.model.dto.rest_api.CreateAgreementRequestDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DelayPenaltyAgreementAdapter implements DelayPenaltyAgreementGateway {

    @Autowired
    private DelayPenaltyAgreementRepository agreementRepository;
    @Autowired
    private DetalleDelayPenaltyAgreementRepository detalleRepository;
    @Autowired
    private AmortizationRepository amortizationRepository;
    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private RecaudoRepository recaudoRepository;

    @Autowired
    ConceptRepository conceptRepository;

    // CONSULTA DE CUOTAS PENDIENTES NO PACTADAS
    @Override
    @Transactional(readOnly = true)
    public List<PendingQuotaForAgreementDto> getPendingQuotasWithPenalties(Long creditId) {

        CreditEntity credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

        List<AmortizationEntity> cuotasPendientes = amortizationRepository
                .findByCreditIdAndPaidFullOrderByQuotaNumberAsc(creditId, "N");

        // Cuotas que ya tienen pacto pendiente — no mostrar
        List<Long> cuotasPactadas = detalleRepository.findPactedCuotaIdsByCreditId(creditId);

        BigDecimal tasaCredito = credit.getTaxValue()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        LocalDate today = LocalDate.now();

        return cuotasPendientes.stream()
                .filter(cuota -> cuota.getExpirationDate().isBefore(today))
                //.filter(cuota -> cuotasPactadas.contains(cuota.getId()))
                .map(cuota -> {
                    BigDecimal totalPagado = recaudoRepository.getTotalByCuotaId(cuota.getId());
                    BigDecimal saldoPendiente = cuota.getQuotaValue()
                            .add(totalPagado)
                            .max(BigDecimal.ZERO);

                    int daysOverdue = (int) ChronoUnit.DAYS.between(cuota.getExpirationDate(), today);

                    BigDecimal periodosVencidos = BigDecimal.valueOf(daysOverdue)
                            .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);

                    BigDecimal delayPenalty = saldoPendiente
                            .multiply(tasaCredito)
                            .multiply(periodosVencidos)
                            .setScale(2, RoundingMode.HALF_UP);

                    return PendingQuotaForAgreementDto.builder()
                            .quotaId(cuota.getId())
                            .quotaNumber(cuota.getQuotaNumber())
                            .expirationDate(String.valueOf(cuota.getExpirationDate()))
                            .quotaValue(cuota.getQuotaValue())
                            .remainingBalance(saldoPendiente)
                            .daysOverdue(daysOverdue)
                            .pastduePeriods(periodosVencidos)
                            .delayPenalty(delayPenalty)
                            .isOverdue(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // CREAR PACTO
    @Override
    @Transactional
    public AgreementResponseDto createAgreement(CreateAgreementRequestDto request) {

        BigDecimal totalProjected = request.getQuotas().stream()
                .map(CreateAgreementRequestDto.QuotaDetailDto::getDelayPenalty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = request.getDiscountValue() != null
                ? request.getDiscountValue()
                : BigDecimal.ZERO;

        if (discount.compareTo(totalProjected) > 0) {
            throw new IllegalArgumentException("El descuento no puede superar el valor proyectado");
        }

        String username = getUsernameToken();
        BigDecimal valorPactado = totalProjected.subtract(discount);

        // Guardar maestro
        DelayPenaltyAgreementEntity master = agreementRepository.save(
                DelayPenaltyAgreementEntity.builder()
                        .creditId(request.getCreditId())
                        .projectedValue(totalProjected)
                        .discountValue(discount)
                        .agreedValue(valorPactado)
                        .status(false)
                        .userCreate(username)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // Guardar detalles
        List<DetalleDelayPenaltyAgreementEntity> detalles = request.getQuotas().stream()
                .map(q -> {
                    // Calcular pastduePeriods desde daysLate si viene null
                    BigDecimal pastduePeriods = q.getPastduePeriods() != null
                            ? q.getPastduePeriods()
                            : BigDecimal.valueOf(q.getDaysLate())
                            .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);

                    return DetalleDelayPenaltyAgreementEntity.builder()
                            .agreement(DelayPenaltyAgreementEntity.builder().id(master.getId()).build())
                            .cuotaId(q.getCuotaId())
                            .daysLate(q.getDaysLate())
                            .pastduePeriods(pastduePeriods)
                            .balancePending(q.getBalancePending())
                            .delayPenalty(q.getDelayPenalty())
                            .userCreate(username)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());

        ConceptEntity concept = conceptRepository.findByConceptKey("ND")
                .orElseThrow(() -> new RuntimeException("Concepto no encontrado"));

        RecaudoEntity recaudoEntity = RecaudoEntity.builder()
                .creditId(request.getCreditId())
                .conceptId(concept.getId())
                .delayPenalty(valorPactado)
                .valuePaid(BigDecimal.valueOf(0))
                .userCreate(username)
                .createdAt(LocalDateTime.now())
                .build();
        recaudoRepository.save(recaudoEntity);

        detalleRepository.saveAll(detalles);

        return buildResponse(master, detalles);
    }

    // ─────────────────────────────────────────────
    // LISTAR PACTOS POR CRÉDITO
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AgreementResponseDto> getAgreementsByCreditId(Long creditId) {
        return agreementRepository.findByCreditId(creditId).stream()
                .map(agreement -> buildResponse(
                        agreement,
                        detalleRepository.findByAgreementId(agreement.getId())
                ))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // ACTUALIZAR ESTADO
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public AgreementResponseDto updateAgreementStatus(Long agreementId, Boolean newStatus) {

        DelayPenaltyAgreementEntity agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Pacto de pago no encontrado"));

        agreement.setStatus(newStatus);
        agreement.setUserUpdate(getUsernameToken());
        agreement.setUpdatedAt(LocalDateTime.now());

        DelayPenaltyAgreementEntity updated = agreementRepository.save(agreement);

        return buildResponse(
                updated,
                detalleRepository.findByAgreementId(updated.getId())
        );
    }

    // ─────────────────────────────────────────────
    // HELPER: construir respuesta
    // ─────────────────────────────────────────────
    private AgreementResponseDto buildResponse(
            DelayPenaltyAgreementEntity master,
            List<DetalleDelayPenaltyAgreementEntity> detalles) {

        List<AgreementDetailResponseDto> detallesDto = detalles.stream()
                .map(d -> {
                    Integer quotaNumber = amortizationRepository.findById(d.getCuotaId())
                            .map(AmortizationEntity::getQuotaNumber)
                            .orElse(null);

                    return AgreementDetailResponseDto.builder()
                            .id(d.getId())
                            .cuotaId(d.getCuotaId())
                            .quotaNumber(quotaNumber)
                            .daysLate(d.getDaysLate())
                            .pastduePeriods(d.getPastduePeriods())
                            .balancePending(d.getBalancePending())
                            .delayPenalty(d.getDelayPenalty())
                            .build();
                })
                .collect(Collectors.toList());

        return AgreementResponseDto.builder()
                .id(master.getId())
                .creditId(master.getCreditId())
                .projectedValue(master.getProjectedValue())
                .discountValue(master.getDiscountValue())
                .agreedValue(master.getAgreedValue())
                .status(master.getStatus())
                .userCreate(master.getUserCreate())
                .createdAt(String.valueOf(master.getCreatedAt()))
                .detalles(detallesDto)
                .build();
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}