package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.CreditIGateway;
import com.recaudo.api.domain.gateway.CreditIntentionStatusGateway;
import com.recaudo.api.domain.mapper.CreditMapper;
import com.recaudo.api.domain.model.constant.CreditStatus;
import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditRegisterDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.ResourceNotFoundException;
import com.recaudo.api.domain.model.constant.CreditStatusCode;
import com.recaudo.api.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreditAdapter implements CreditIGateway {

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private AmortizationRepository amortizationRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CreditIntentionStatusGateway creditIntentionStatusGateway;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClosingRepository closingRepository;

    @Autowired
    private CreditLineRepository creditLineRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CreditIntentionAmortizationNRepository creditIntentionAmortizationNRepository;

    @Autowired
    private CreditIntentionAmortizationDetailRepository creditIntentionAmortizationDetailRepository;

    @Autowired
    private CreditAmortizationNRepository  creditAmortizationRepository;

    @Autowired
    private CreditAmortizationDetailRepository creditAmortizationDetailRepository;

    @Autowired
    private CreditRatingRangeRepository creditRatingRangeRepository;


    @Autowired(required = false)
    CreditMapper creditMapper = Mappers.getMapper(CreditMapper.class);


    @Override
    public List<CreditFullResponseDto> getAll() {
        try {
            return creditRepository.findAllCreditsFull().stream()
                    .map(this::mapView)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener los créditos", e);
            throw new RuntimeException("Error al obtener los créditos", e);
        }
    }

    private CreditRatingDTO calcularCalificacion(Integer diasMora) {
        if (diasMora == null || diasMora < 0) diasMora = 0;

        return creditRatingRangeRepository.findByDiasMora(diasMora)
                .map(r -> new CreditRatingDTO(r.getRatingValue(), r.getStart(), r.getEnd()))
                .orElse(new CreditRatingDTO("N/A", null, null));
    }

    @Override
    public List<CreditFullResponseDto> getByUsername(String username) {
        try {
            return creditRepository.findCreditsByUsername(username).stream()
                    .map(this::mapView)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener los créditos", e);
            throw new RuntimeException("Error al obtener los créditos", e);
        }
    }

    @Override
    public CreditResponseDto getById(Long id) {
        try {
            CreditResponseDto credit = creditRepository.findBy(id);
            if (credit == null) {
                throw new BadRequestException(
                        messageSource.getMessage("credit.not.found", null, Locale.getDefault())
                );
            }
            return credit;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener el crédito por id: {}", id, e);
            throw new RuntimeException("Error al obtener el crédito", e);
        }
    }

    @Override
    public List<CreditProjection> getByPersonId(Long personId) {
            List<CreditProjection> projections =
                    creditRepository.findCreditDetailsByPersonId(personId);

            return projections;
        }

    @Transactional
    @Override
    public CreditResponseDto create(CreditRegisterDto dto) {
        try {
            // Validar que no exista un crédito para esta intención
            if (creditRepository.existsByCreditIntentionId(dto.getCreditIntentionId())) {
                throw new BadRequestException(
                        messageSource.getMessage("credit.already.exists", null, Locale.getDefault())
                );
            }

            // Crear la entidad del crédito
            CreditEntity entity = CreditEntity.builder()
                    .creditIntentionId(dto.getCreditIntentionId())
                    .personId(dto.getPersonId())
                    .creditLineId(dto.getCreditLineId())
                    .quotaValue(dto.getQuotaValue())
                    .periodId(dto.getPeriodId())
                    .periodQuantity(dto.getPeriodQuantity())
                    .taxTypeId(dto.getTaxTypeId())
                    .taxValue(dto.getTaxValue())
                    .totalIntentionValue(dto.getTotalIntentionValue())
                    .totalInterestValue(dto.getTotalInterestValue())
                    .totalCapitalValue(dto.getTotalCapitalValue())
                    .itemValue(dto.getItemValue())
                    .initialValuePayment(dto.getInitialValuePayment())
                    .totalFinancedValue(dto.getTotalFinancedValue())
                    .stationery(dto.getStationery())
                    .userCreate(getUsernameToken())
                    .createdAt(LocalDateTime.now())
                    .creditStatus(CreditStatus.ACTIVE)
                    .build();

            // Guardar el crédito
            CreditEntity saved = creditRepository.save(entity);
            log.info("Crédito creado exitosamente con ID: {}", saved.getId());

            // Insertar la tabla de amortización
            insertToCreditAmortization(dto.getCreditIntentionId(), saved.getId());

            //Actualizar estado de la intencion de credito
            ChangeCreditStatusDto newStatus = new ChangeCreditStatusDto();
            newStatus.setCreditId(dto.getCreditIntentionId());
            newStatus.setNewStatus(CreditStatusCode.TERMINATED);
            newStatus.setActivity("GENERACION DE CREDITO");
            newStatus.setObservation("SE CREO CREDITO EXITOSAMENTE");
            CreditIntentionStatusResponseDto response = updateStatusCreditIntention(newStatus);

            // Retornar DTO
            return creditMapper.entityToDto(saved);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear el crédito", e);
            throw new RuntimeException("Error al crear el crédito: " + e.getMessage(), e);
        }
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    private CreditIntentionStatusResponseDto updateStatusCreditIntention(ChangeCreditStatusDto dto) {
        return creditIntentionStatusGateway.updateStatus(dto);
    }

    @Transactional()
    private void insertToCreditAmortization(Long creditIntentionId, Long creditId) {
        try {

            // leer maestros de la Intención
            List<CreditIntentionAmortizationNEntity> origen =
                    creditIntentionAmortizationNRepository
                            .findByCreditIntentionIdOrderByQuotaNumber(creditIntentionId);

            if (origen == null || origen.isEmpty()) {
                log.warn("No existe amortización para la intención ID: {} — " +
                        "verifique que la intención haya sido calculada.", creditIntentionId);
                return;
            }

            // construir maestros de Crédito
            List<CreditAmortizationNEntity> maestrosCredito = origen.stream()
                    .map(o -> CreditAmortizationNEntity.builder()
                            .creditId(creditId)
                            .quotaNumber(o.getQuotaNumber())
                            .expirationDate(o.getExpirationDate())
                            .totalQuotaValue(o.getTotalQuotaValue())
                            .liquidated("N")
                            .paidFull("N")
                            .build())
                    .collect(Collectors.toList());

            creditAmortizationRepository.saveAll(maestrosCredito);

            log.info("Maestros credit_amortization guardados: {} cuotas — crédito ID: {}",
                    maestrosCredito.size(), creditId);

            // construir detalles de Crédito
            List<CreditAmortizationDetailEntity> detallesCredito = new ArrayList<>();

            for (int i = 0; i < origen.size(); i++) {
                Long origenMaestroId  = origen.get(i).getId();
                Long destinoMaestroId = maestrosCredito.get(i).getId();

                List<CreditIntentionAmortizationDetailEntity> detallesOrigen =
                        creditIntentionAmortizationDetailRepository
                                .findByAmortizationId(origenMaestroId);

                if (detallesOrigen.isEmpty()) {
                    log.warn("Sin detalles en origen para cuota origen_id: {} (intención ID: {})",
                            origenMaestroId, creditIntentionId);
                    continue;
                }

                for (CreditIntentionAmortizationDetailEntity detOrigen : detallesOrigen) {
                    detallesCredito.add(CreditAmortizationDetailEntity.builder()
                            .amortizationId(destinoMaestroId)
                            .conceptId(detOrigen.getConceptId())
                            .value(detOrigen.getValue())
                            .build());
                }
            }

            creditAmortizationDetailRepository.saveAll(detallesCredito);

            log.info("Detalles credit_amortization_detail guardados: {} registros — crédito ID: {}",
                    detallesCredito.size(), creditId);

        } catch (Exception e) {
            log.error("Error al replicar amortización — intención ID: {}, crédito ID: {}",
                    creditIntentionId, creditId, e);
            throw new RuntimeException(
                    "Error al procesar la amortización del crédito ID: " + creditId, e);
        }
    }

    @Override
    public List<CreditFullResponseDto> getByAsesorUsername(String username) {
        try {
            return creditRepository.findCreditsByUsername(username).stream()
                    .map(this::mapView)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener créditos del asesor: {}", username, e);
            throw new RuntimeException("Error al obtener créditos del asesor", e);
        }
    }

    @Override
    public List<CreditFullResponseDto> getByAsesorZone(Long personId) {
        Long zona = personRepository.getZonasIdByAsesor(personId).stream().findFirst().orElse(null);
        if (zona == null) return List.of();
        try {
            return creditRepository.findCreditsByZona(zona).stream()
                    .map(this::mapView)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener créditos de la zona ID: {}", zona, e);
            throw new RuntimeException("Error al obtener créditos del asesor", e);
        }
    }

    //SERVICIO PARA OBTENER CREDITOS CAUSADOS EL DIA DE HOY ASOCIADOS A UN ASESOR
    @Override
    public List<CreditCausadoProjection> getCreditsCausadosByClosing(Long closingId) {
        // 1. Obtener el cierre
        ClosingEntity closing = closingRepository.findById(closingId)
                .orElseThrow(() -> new ResourceNotFoundException("Cierre no encontrado"));

        // 2. Obtener el username del asesor por su person_id
        UserEntity user = userRepository.findByPersonId(closing.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para esta persona"));

        // 3. Obtener las líneas de desembolso
        List<CreditLineEntity> creditLines = creditLineRepository.findByLoanDisbursementIsTrue();
        List<Long> ids = creditLines.stream().map(CreditLineEntity::getId).toList();

        // 3. Buscar créditos causados ese día por ese asesor
        return creditRepository.findCreditsCausadosByAsesorAndDateAndCreditLine(
                user.getUsername(),
                closing.getClosingDate(),
                ids
        );
    }

    @Override
    public CierreAsesorView findCierreAsesor(Long personId, Long creditId) {
        return creditRepository.findCierreAsesorAndZona(personId, creditId);
    }

    // ── helper privado (agrégalo una sola vez en la clase) ──────────────────────
    private CreditFullResponseDto mapView(CreditFullView v) {
        CreditFullResponseDto dto = new CreditFullResponseDto();
        dto.setId(v.getId());
        dto.setCreditIntentionId(v.getCreditIntentionId());
        dto.setCreditStatus(v.getCreditStatus());
        dto.setQuotaValue(v.getQuotaValue());
        dto.setPeriodQuantity(v.getPeriodQuantity());
        dto.setTotalIntentionValue(v.getTotalIntentionValue());
        dto.setTotalInterestValue(v.getTotalInterestValue());
        dto.setTotalCapitalValue(v.getTotalCapitalValue());
        dto.setTotalFinancedValue(v.getTotalFinancedValue());
        dto.setZoneId(v.getZoneId());
        dto.setZoneName(v.getZoneName());
        dto.setDocument(v.getDocument());
        dto.setFullname(v.getFullname());
        dto.setPhoneNumber(v.getPhoneNumber());
        dto.setCreditLineId(v.getCreditLineId());
        dto.setCreditLineName(v.getCreditLineName());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setRatingCredit(calcularCalificacion(v.getDiasMora()));
        return dto;
    }

}