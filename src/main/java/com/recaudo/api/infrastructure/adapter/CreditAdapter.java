package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.CreditIGateway;
import com.recaudo.api.domain.gateway.CreditIntentionStatusGateway;
import com.recaudo.api.domain.mapper.CreditMapper;
import com.recaudo.api.domain.model.dto.response.CreditCausadoProjection;
import com.recaudo.api.domain.model.dto.response.CreditFullResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditIntentionStatusResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditProjection;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditRegisterDto;
import com.recaudo.api.domain.model.entity.AmortizationEntity;
import com.recaudo.api.domain.model.entity.ClosingEntity;
import com.recaudo.api.domain.model.entity.CreditEntity;
import com.recaudo.api.domain.model.entity.CreditIntentionAmortizationEntity;
import com.recaudo.api.domain.model.entity.UserEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.ResourceNotFoundException;
import com.recaudo.api.domain.model.constant.CreditStatusCode;
import com.recaudo.api.infrastructure.repository.AmortizationRepository;
import com.recaudo.api.infrastructure.repository.ClosingRepository;
import com.recaudo.api.infrastructure.repository.CreditIntentionAmortizationRepository;
import com.recaudo.api.infrastructure.repository.CreditRepository;
import com.recaudo.api.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class CreditAdapter implements CreditIGateway {

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private CreditIntentionAmortizationRepository creditIntentionAmortizationRepository;

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

    @Autowired(required = false)
    CreditMapper creditMapper = Mappers.getMapper(CreditMapper.class);


    @Override
    public List<CreditFullResponseDto> getAll() {
        try {
            return creditRepository.findAllCreditsFull();
        } catch (Exception e) {
            log.error("Error al obtener los créditos", e);
            throw new RuntimeException("Error al obtener los créditos", e);
        }
    }

    @Override
    public List<CreditFullResponseDto> getByUsername(String username) {
        try {
            return creditRepository.findCreditsByUsername(username);
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


    @Transactional
    private void insertToCreditAmortization(Long creditIntentionId, Long creditId) {
        try {
            // Obtener la amortización proyectada de la intención de crédito
            List<CreditIntentionAmortizationEntity> proyecciones =
                    creditIntentionAmortizationRepository.findByCreditIntencionId(creditIntentionId);

            if (proyecciones == null || proyecciones.isEmpty()) {
                log.warn("No se encontró amortización para la intención de crédito ID: {}", creditIntentionId);
                return;
            }

            // Crear la lista de amortizaciones para el crédito
            List<AmortizationEntity> amortizationList = new ArrayList<>();

            for (CreditIntentionAmortizationEntity proyeccion : proyecciones) {
                AmortizationEntity amortization = AmortizationEntity.builder()
                        .creditId(creditId)
                        .quotaNumber(proyeccion.getQuotaNumber())
                        .expirationDate(proyeccion.getExpirationDate())
                        .capitalBalance(BigDecimal.valueOf(proyeccion.getCapitalBalance()))
                        .investmentValue(BigDecimal.valueOf(proyeccion.getInvestmentValue()))
                        .interestValue(BigDecimal.valueOf(proyeccion.getInterestValue()))
                        .lifeInsurance(BigDecimal.valueOf(proyeccion.getLifeInsurance()))
                        .portfolioInsurance(BigDecimal.valueOf(proyeccion.getPortfolioInsurance()))
                        .liquidated("N")
                        .paidFull("N")
                        .quotaValue(BigDecimal.valueOf(proyeccion.getQuotaValue()))
                        .build();

                amortizationList.add(amortization);
            }

            // Guardar todos los registros de amortización
            amortizationRepository.saveAll(amortizationList);

            log.info("Se insertaron {} registros de amortización para el crédito ID: {}",
                    amortizationList.size(), creditId);

        } catch (Exception e) {
            log.error("Error inesperado al insertar amortización para el crédito ID: {}", creditId, e);
            throw new RuntimeException("Error al procesar la amortización del crédito", e);
        }
    }

    @Override
    public List<CreditProjection> getByAsesorUsername(String username) {
        try {
            return creditRepository.findActiveCreditsByAsesorUsername(username);
        } catch (Exception e) {
            log.error("Error al obtener créditos del asesor: {}", username, e);
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

        // 3. Buscar créditos causados ese día por ese asesor
        return creditRepository.findCreditsCausadosByAsesorAndDate(
                user.getUsername(),
                closing.getClosingDate()
        );
    }
}