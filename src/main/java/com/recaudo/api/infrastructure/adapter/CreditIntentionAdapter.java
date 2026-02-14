    package com.recaudo.api.infrastructure.adapter;

    import com.fasterxml.jackson.databind.node.JsonNodeFactory;
    import com.fasterxml.jackson.databind.node.ObjectNode;
    import com.recaudo.api.domain.gateway.CreditIntentionGateway;
    import com.recaudo.api.domain.gateway.CreditIntentionStatusGateway;
    import com.recaudo.api.domain.mapper.CreditIntentionMapper;
    import com.recaudo.api.domain.model.dto.response.CreditIntentionResponseDto;
    import com.recaudo.api.domain.model.dto.response.IntentionCreditResponseAllDto;
    import com.recaudo.api.domain.model.dto.response.ProyeccionAmortizacionDto;
    import com.recaudo.api.domain.model.dto.rest_api.CalculateCreditIntentionDto;
    import com.recaudo.api.domain.model.dto.rest_api.ClientDataCreditIntentionUpdateDto;
    import com.recaudo.api.domain.model.dto.rest_api.CreditIntentionDto;
    import com.recaudo.api.domain.model.dto.rest_api.CreditIntentionUpdateDto;
    import com.recaudo.api.domain.model.dto.rest_api.UpdateFechaTentativaCreditIntentionDto;
    import com.recaudo.api.domain.model.entity.*;
    import com.recaudo.api.exception.BadRequestException;
    import com.recaudo.api.exception.CreditSimulationException;
    import com.recaudo.api.infrastructure.helper.util.CreditStatusCode;
    import com.recaudo.api.infrastructure.repository.*;
    import jakarta.transaction.Transactional;
    import lombok.extern.slf4j.Slf4j;
    import org.mapstruct.factory.Mappers;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.MessageSource;
    import org.springframework.dao.DataAccessException;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Locale;
    import java.util.Optional;

    @Slf4j
    @Service
    public class CreditIntentionAdapter implements CreditIntentionGateway {

        @Autowired
        private CreditIntentionRepository creditIntentionRepository;

        @Autowired
        private CreditLineRepository creditLineRepository;

        @Autowired
        private TaxTypeRepository taxTypeRepository;

        @Autowired
        private PeriodRepository periodRepository;

        @Autowired
        private ZonaRepository zonaRepository;

        private BarrioRepository barrioRepository;

        @Autowired
        private PersonRepository personRepository;

        @Autowired
        private CreditIntentionAmortizationRepository creditIntentionAmortizationRepository;

        @Autowired
        CreditLineServiceQuotaRepository creditLineServiceQuotaRepository;

        @Autowired
        MessageSource messageSource;

        @Autowired
        CreditIntentionStatusGateway creditIntentionStatusGateway;

        @Autowired
        private  CreditIntentionStatusRepository creditIntentionStatusRepository;


        @Autowired(required = false)
        CreditIntentionMapper creditIntentionMapper = Mappers.getMapper(CreditIntentionMapper.class);


        @Override
        public List<IntentionCreditResponseAllDto> getAll() {

            try {
                return creditIntentionRepository.findAllCreditIntentions();
            } catch (Exception e) {
                log.error("Error al obtener las últimas intenciones de crédito", e);
                throw new RuntimeException("Error al obtener las últimas intenciones de crédito", e);
            }
        }

        @Override
        public List<IntentionCreditResponseAllDto> getById(Long id) {
            try {
                return creditIntentionRepository.findByIdProjection(id);
            } catch (Exception e) {
                log.error("Error al obtener la intención de crédito por id", e);
                throw new RuntimeException("Error al obtener la intención de crédito", e);
            }
        }

        @Transactional
        @Override
        public CreditIntentionResponseDto create(CreditIntentionDto creditIntentionDto) {

            validacionIntencionCredito(creditIntentionDto);
            CreditIntentionEntity entity = creditIntentionMapper.dtoToEntity(creditIntentionDto);

            //PAPELERIA
            double originalItemValue = creditIntentionDto.getItemValue();
            BigDecimal stationeryValueToSave = BigDecimal.ZERO;

// Consultar todos los service quota con capitalización
            List<CreditLineServiceQuotaEntity> capitalizableQuotas =
                    creditLineServiceQuotaRepository.findByCreditLineIdAndCapitalizeTrue(
                            creditIntentionDto.getCreditLineId()
                    );

// Filtrar solo papelería (serviceQuotaId = 7)
            Optional<CreditLineServiceQuotaEntity> stationeryQuota = capitalizableQuotas.stream()
                    .filter(quota -> quota.getServiceQuotaId() != null && quota.getServiceQuotaId() == 7)
                    .findFirst();

            if (stationeryQuota.isPresent()) {
                // Calcular el 1% de papelería
                double stationeryCalculated = originalItemValue * 0.01;
                stationeryValueToSave = BigDecimal.valueOf(stationeryCalculated);

                log.info("Papelería aplicada - Item Value: {}, Papelería (1%): {}",
                        originalItemValue, stationeryCalculated);
            }

            entity.setZoneId(creditIntentionDto.getZoneId());
            entity.setCreditLineId(creditIntentionDto.getCreditLineId());
            entity.setPeriodId(creditIntentionDto.getPeriodId());
            entity.setTaxTypeId(creditIntentionDto.getTaxTypeId());
            entity.setTotalCapitalValue(
                    creditIntentionDto.getTotalFinancedValue() == 0
                            ? BigDecimal.valueOf(creditIntentionDto.getItemValue())
                            : BigDecimal.valueOf(creditIntentionDto.getTotalFinancedValue())
            );
            entity.setItemValue(
                    creditIntentionDto.getTotalFinancedValue() != 0
                            ? BigDecimal.valueOf(creditIntentionDto.getItemValue())
                            : BigDecimal.ZERO
            );
            entity.setStationery(stationeryValueToSave);
            entity.setEndQuincena(creditIntentionDto.getFinQuincena());
            entity.setInitialQuincena(creditIntentionDto.getInicioQuincena());
            entity.setTotalInterestValue(BigDecimal.ZERO);
            entity.setUserCreate(getUsernameToken());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setDateStart(LocalDate.parse(creditIntentionDto.getStartDate()));

            CreditIntentionEntity savedEntity = creditIntentionRepository.save(entity);

            //LLAMADA AL PROCEDIMIENTO Y INSERTAR EN AMORTIZACION
            PeriodEntity period = periodRepository.findById(
                    creditIntentionDto.getPeriodId()
            ).orElseThrow(() -> new BadRequestException("Período no existe"));

            CalculateCreditIntentionDto calculateDto =
                    buildCalculateDto(creditIntentionDto, period);

            insertToIntentionAmortization(calculateDto, savedEntity.getId());
            creditIntentionStatusGateway.create(savedEntity.getId(), savedEntity.getUserCreate(),CreditStatusCode.STUDY );

            return creditIntentionMapper.entityToDto(savedEntity);
        }

        //CONSTRUIR DTO PARA INSERTAR AMORTIZACION POR PRIMERA VEZ
        private CalculateCreditIntentionDto buildCalculateDto(
                CreditIntentionDto creditDto,
                PeriodEntity period
        ) {
            return CalculateCreditIntentionDto.builder()
                    .creditLineId(creditDto.getCreditLineId())
                    .periodCode(period.getCod())
                    .periodQuantity(creditDto.getPeriodQuantity())
                    .itemValue(creditDto.getItemValue())
                    .quotaValue(creditDto.getQuotaValue())
                    .taxValue(creditDto.getTaxValue())
                    .inicioQuincena(creditDto.getInicioQuincena())
                    .finQuincena(creditDto.getFinQuincena())
                    .tipoCalculo("CALCULAR_CUOTA")
                    .generarAmortizacion("SI")
                    .startDate(creditDto.getStartDate())
                    .build();
        }

        //CONSTRUIR DTO PARA INSERTAR AMORTIZACION POR SEGUNDA VEZ
        private CalculateCreditIntentionDto buildCalculateDtoForUpdate(
                CreditIntentionEntity entity,
                CreditIntentionUpdateDto dto,
                PeriodEntity period
        )   {
            return CalculateCreditIntentionDto.builder()
                    .creditLineId(
                            dto.getCreditLineId() != null
                                    ? dto.getCreditLineId()
                                    : entity.getCreditLineId())
                    .periodCode(period.getCod())
                    .periodQuantity(
                            dto.getPeriodQuantity() != null
                                    ? dto.getPeriodQuantity()
                                    : entity.getPeriodQuantity().intValue())
                    .itemValue(
                            dto.getItemValue() != null
                                    ? dto.getItemValue()
                                    : entity.getItemValue().doubleValue())
                    .quotaValue(
                            dto.getQuotaValue() != null
                                    ? dto.getQuotaValue()
                                    : entity.getQuotaValue().doubleValue())
                    .taxValue(
                            dto.getTaxValue() != null
                                    ? dto.getTaxValue()
                                    : entity.getTaxValue().doubleValue())
                    .inicioQuincena(
                            dto.getInicioQuincena() != null
                                    ? dto.getInicioQuincena()
                                    : entity.getInitialQuincena())
                    .finQuincena(
                            dto.getFinQuincena() != null
                                    ? dto.getFinQuincena()
                                    : entity.getEndQuincena())
                    .tipoCalculo(
                            dto.getTipoCalculo() != null
                                    ? dto.getTipoCalculo()
                                    : "CALCULAR_CUOTA")
                    .startDate(
                            dto.getStartDate() != null
                                    ? dto.getStartDate()
                                    : entity.getCreatedAt().toString())
                    .generarAmortizacion("SI")
                    .build();
        }



        @Transactional
        private void insertToIntentionAmortization(CalculateCreditIntentionDto data, Long id){
            try {
                // Construir el DTO para la simulación
                CalculateCreditIntentionDto calculateDto = new CalculateCreditIntentionDto();
                calculateDto.setCreditLineId(data.getCreditLineId());
                calculateDto.setPeriodCode(data.getPeriodCode());
                calculateDto.setTipoCalculo(data.getTipoCalculo());
                calculateDto.setPeriodQuantity(data.getPeriodQuantity());
                calculateDto.setItemValue(data.getItemValue());
                calculateDto.setQuotaValue(data.getQuotaValue());
                calculateDto.setTaxValue(data.getTaxValue());
                calculateDto.setInicioQuincena(data.getInicioQuincena());
                calculateDto.setFinQuincena(data.getFinQuincena());
                calculateDto.setStartDate(data.getStartDate());
                calculateDto.setGenerarAmortizacion("SI");

                List<ProyeccionAmortizacionDto> proyeccion = this.simulate(calculateDto);

                if (proyeccion == null || proyeccion.isEmpty()) {
                    log.warn("No se obtuvo proyección");
                    return;
                }

                BigDecimal totalInterestValue = BigDecimal.ZERO;

                // Registro en la tabla credit_intention_amortizacion
                List<CreditIntentionAmortizationEntity> amortizationList = new ArrayList<>();

                for (ProyeccionAmortizacionDto proyeccionDto : proyeccion) {
                    CreditIntentionAmortizationEntity amortization = new CreditIntentionAmortizationEntity();

                    amortization.setCreditIntencionId(id);
                    amortization.setQuotaNumber(proyeccionDto.getDcreNumcuota());
                    amortization.setCapitalBalance(proyeccionDto.getDcreSaldocapital());
                    amortization.setInvestmentValue(proyeccionDto.getDcreVlrabonoinversion());
                    amortization.setInterestValue(proyeccionDto.getDcreVlrabonointeres());
                    amortization.setLifeInsurance(proyeccionDto.getDcreVlrabonosegurovida());
                    amortization.setPortfolioInsurance(proyeccionDto.getDcreVlrabonosegurocartera());
                    amortization.setExpirationDate(proyeccionDto.getDcreFvence());
                    amortization.setQuotaValue(proyeccionDto.getDcreVlrcuota());
                    amortization.setLiquidated("N");

                    amortizationList.add(amortization);
                    // Sumar el valor de interés verificando que no sea null
                    if (proyeccionDto.getDcreVlrabonointeres() != null) {
                        totalInterestValue = totalInterestValue.add(BigDecimal.valueOf(proyeccionDto.getDcreVlrabonointeres()));
                    }
                }

                // Guardar todos los registros
                creditIntentionAmortizationRepository.saveAll(amortizationList);

                log.info("Se insertaron {} registros de amortización para la intención de crédito ID: {}",
                        amortizationList.size(), id);

                CreditIntentionEntity creditIntention = creditIntentionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Intención de crédito no encontrada"));

                creditIntention.setTotalInterestValue(totalInterestValue);
                creditIntentionRepository.save(creditIntention);

                log.info("Total de intereses actualizado: {} para la intención de crédito ID: {}",
                        totalInterestValue, id);

            } catch (CreditSimulationException e) {
                log.error("Error al simular la proyección para insertar amortización: {}",
                        e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("Error inesperado al insertar amortización para intención de crédito", e);
                throw new RuntimeException("Error al procesar la amortización del crédito", e);
            }

        }

        //SIMULACION DE INTENCION DE CREDITO
        @Override
        public List<ProyeccionAmortizacionDto> simulate(CalculateCreditIntentionDto dto) {

            try {

                double itemValueToSimulate = dto.getItemValue();
                double stationeryValue = 0.0;

                List<CreditLineServiceQuotaEntity> capitalizableQuotas =
                        creditLineServiceQuotaRepository.findByCreditLineIdAndCapitalizeTrue(dto.getCreditLineId());

                // Filtrar solo papelería (serviceQuotaId = 7)
                Optional<CreditLineServiceQuotaEntity> stationeryQuota = capitalizableQuotas.stream()
                        .filter(quota -> quota.getServiceQuotaId() != null && quota.getServiceQuotaId() == 7)
                        .findFirst();

                if (stationeryQuota.isPresent()) {
                    // Calcular el 1% de papelería
                    stationeryValue = dto.getItemValue() * 0.01;
                    // Sumar al itemValue para la simulación
                    itemValueToSimulate = dto.getItemValue() + stationeryValue;
                }

                //PREPARACION DEL JSON ENVIADO AL PROCEDIMIENTO ALMACENADO
                ObjectNode json = JsonNodeFactory.instance.objectNode();

                json.put("id_linea", dto.getCreditLineId());
                json.put("id_periodo", dto.getPeriodCode());
                json.put("id_tipo_calculo", dto.getTipoCalculo());
                json.put("id_plazo", dto.getPeriodQuantity());
                json.put("id_capital", itemValueToSimulate);
                json.put("id_edad", 0);
                json.put("id_cuota", dto.getQuotaValue());
                json.put("id_tasa", dto.getTaxValue());
                json.put("id_dia_inicial_quincena",
                        dto.getInicioQuincena() != null ? dto.getInicioQuincena() : 0);
                json.put("id_dia_final_quincena",
                        dto.getFinQuincena() != null ? dto.getFinQuincena() : 0);
                json.put("id_fecha_inicio",
                        dto.getStartDate() != null
                                ? dto.getStartDate()
                                : LocalDate.now().toString());
                json.put("id_generar_amortizacion",
                        dto.getGenerarAmortizacion() != null
                                ? dto.getGenerarAmortizacion()
                                : "SI");

                return creditIntentionRepository
                        .ejecutarProyeccion(json.toString());

            }
            // Error del procedimiento / función SQL
            catch (DataAccessException e) {
                String[] errorParts = e.getMessage().split("'");
                String messageCode = Arrays.stream(errorParts)
                        .filter( element -> element.contains("recaudo."))
                        .map(element -> element.split("\\.")[1])
                        .findFirst().orElse("DEFAULT_ERROR");

                String message = messageSource.getMessage(messageCode, null, Locale.getDefault());

                e.getMostSpecificCause();
                String rootCause = e.getMostSpecificCause().getMessage();

                //log.error("Error de BD al ejecutar proyección. Causa: {}", rootCause, e);

                throw new CreditSimulationException(
                        message,
                        e
                );
            }
            // Datos inválidos
            catch (IllegalArgumentException | NullPointerException e) {
                log.error("Datos inválidos para la simulación. DTO: {}", dto, e);

                throw new CreditSimulationException(
                        "Datos inválidos para la simulación del crédito",
                        e
                );
            }
            // error inesperado
            catch (Exception e) {
                log.error("Error inesperado al simular crédito", e);

                throw new CreditSimulationException(
                        "Error inesperado al simular la proyección del crédito",
                        e
                );
            }
        }

        @Override
        public boolean existById(Long id) {
            return creditIntentionRepository.existsById(id);
        }


        //ACTUALIZAR DATOS DEL CLIENTE EN UNA INTENCION DE CREDITO EXISTENTE
        @Override
        public CreditIntentionResponseDto updateDataClient(Long id, ClientDataCreditIntentionUpdateDto dto) {

            CreditIntentionEntity intention = creditIntentionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Intención de crédito no encontrada"));

            // ===============================
            // SOLO DATOS DEL CLIENTE
            // ===============================

            intention.setZoneId(dto.getZoneId());
            intention.setDocumentType(dto.getDocumentType());
            intention.setDocument(dto.getDocument());

            intention.setFirstname(dto.getFirstname());
            intention.setMiddlename(dto.getMiddlename());
            intention.setLastname(dto.getLastname());
            intention.setMaternalLastname(dto.getMaternalLastname());
            intention.setFullname(dto.getFullname());

            intention.setGender(dto.getGender());
            intention.setOccupation(dto.getOccupation());
            intention.setDescription(dto.getDescription());

            intention.setEmail(dto.getEmail());
            intention.setPhoneNumber(dto.getPhoneNumber());
            intention.setWhatsappNumber(dto.getWhatsappNumber());

            intention.setHomeAddress(dto.getHomeAddress());
            intention.setCountryId(dto.getCountryId());
            intention.setDepartmentId(dto.getDepartmentId());
            intention.setMunicipalityId(dto.getMunicipalityId());
            intention.setNeighborhoodId(dto.getNeighborhoodId());

            intention.setReferido(dto.getReferido());
            intention.setCallSuccess(dto.getCallSuccess());

            // Auditoría básica
            intention.setEditedAt(LocalDateTime.now());
            intention.setUserEdit(getUsernameToken());

            CreditIntentionEntity updated = creditIntentionRepository.save(intention);

            return creditIntentionMapper.entityToDto(updated);
        }

        @Override
        public CreditIntentionResponseDto updateFechaTentativaCreditIntention(Long id, UpdateFechaTentativaCreditIntentionDto dto) {
            CreditIntentionEntity intention = creditIntentionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Intención de crédito no encontrada"));

            System.out.println("Fecha recibida: " + dto.getStartdate());
            intention.setDateStart(LocalDate.parse(dto.getStartdate()));

            CreditIntentionEntity updated = creditIntentionRepository.save(intention);
            return creditIntentionMapper.entityToDto(updated);
        }

        //ACTUALIZAR DATOS DE LA SOLICITUD DE INTENCION DE CREDITO EN UNA INTENCION DE CREDITO EXISTENTE
        @Transactional
        @Override
        public CreditIntentionResponseDto updateDataCreditIntention(
                Long id,
                CreditIntentionUpdateDto dto
        ) {

            CreditIntentionEntity intention = creditIntentionRepository.findById(id)
                    .orElseThrow(() ->
                            new BadRequestException("Intención de crédito no encontrada")
                    );

            // LÓGICA DE PAPELERÍA
            BigDecimal stationeryValueToSave = intention.getStationery() != null
                    ? intention.getStationery()
                    : BigDecimal.ZERO;

            // Si se está actualizando el itemValue O creditLineId, recalcular papelería
            // Si se está actualizando el itemValue O creditLineId, recalcular papelería
            if (dto.getItemValue() != null || dto.getCreditLineId() != null) {
                Long creditLineId = dto.getCreditLineId() != null
                        ? dto.getCreditLineId()
                        : intention.getCreditLineId();

                Double itemValue = dto.getItemValue() != null
                        ? dto.getItemValue()
                        : intention.getItemValue().doubleValue();

                List<CreditLineServiceQuotaEntity> capitalizableQuotas =
                        creditLineServiceQuotaRepository.findByCreditLineIdAndCapitalizeTrue(creditLineId);

                // Filtrar solo papelería (serviceQuotaId = 7)
                Optional<CreditLineServiceQuotaEntity> stationeryQuota = capitalizableQuotas.stream()
                        .filter(quota -> quota.getServiceQuotaId() != null && quota.getServiceQuotaId() == 7)
                        .findFirst();

                if (stationeryQuota.isPresent()) {
                    double stationeryCalculated = itemValue * 0.01;
                    stationeryValueToSave = BigDecimal.valueOf(stationeryCalculated);

                    log.info("Papelería recalculada - Item Value: {}, Papelería: {}",
                            itemValue, stationeryCalculated);
                } else {
                    // Si ya no tiene papelería, limpiar el valor
                    stationeryValueToSave = BigDecimal.ZERO;
                }
            }

            // ACTUALIZAR CAMPOS

            if (dto.getCreditLineId() != null)
                intention.setCreditLineId(dto.getCreditLineId());

            if (dto.getQuotaValue() != null)
                intention.setQuotaValue(BigDecimal.valueOf(dto.getQuotaValue()));

            if (dto.getPeriodId() != null)
                intention.setPeriodId(dto.getPeriodId());

            if (dto.getPeriodQuantity() != null)
                intention.setPeriodQuantity(dto.getPeriodQuantity().longValue());

            if (dto.getTaxTypeId() != null)
                intention.setTaxTypeId(dto.getTaxTypeId());

            if (dto.getTaxValue() != null)
                intention.setTaxValue(BigDecimal.valueOf(dto.getTaxValue()));

            if (dto.getItemValue() != null)
                intention.setItemValue(BigDecimal.valueOf(dto.getItemValue()));

            if (dto.getInitialValuePayment() != null)
                intention.setInitialValuePayment(BigDecimal.valueOf(dto.getInitialValuePayment()));

            if (dto.getTotalFinancedValue() != null)
                intention.setTotalFinancedValue(BigDecimal.valueOf(dto.getTotalFinancedValue()));

            if (dto.getTotalIntentionValue() != null)
                intention.setTotalIntentionValue(BigDecimal.valueOf(dto.getTotalIntentionValue()));

            if (dto.getInicioQuincena() != null)
                intention.setInitialQuincena(dto.getInicioQuincena());

            if (dto.getFinQuincena() != null)
                intention.setEndQuincena(dto.getFinQuincena());

            intention.setStationery(stationeryValueToSave);
            intention.setUserEdit(getUsernameToken());
            intention.setEditedAt(LocalDateTime.now());

            creditIntentionRepository.save(intention);

            // RECALCULAR AMORTIZACIÓN

            PeriodEntity period = periodRepository.findById(intention.getPeriodId())
                    .orElseThrow(() ->
                            new BadRequestException("Período no existe")
                    );

            CalculateCreditIntentionDto calculateDto =
                    buildCalculateDtoForUpdate(intention, dto, period);

            creditIntentionAmortizationRepository
                    .deleteByCreditIntentionId(id);

            insertToIntentionAmortization(calculateDto, id);

            return creditIntentionMapper.entityToDto(intention);
        }


        private String getUsernameToken() {
            return ((UserDetailsImpl) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal())
                    .getUsername();
        }

        //METODO PARA VALIDAR DATOS PARA INSERCION DE INTENCION DE CREDITO
        private void validacionIntencionCredito(CreditIntentionDto data){
            // --- Validaciones ---
            Optional<ZonaEntity> zonaExist = zonaRepository.findById(data.getZoneId());
            if (zonaExist.isEmpty())
                throw new BadRequestException("No existe la zona");

            Optional<CreditLineEntity> creditLineExist = creditLineRepository.findById(data.getCreditLineId());
            if (creditLineExist.isEmpty())
                throw new BadRequestException("No existe la línea de crédito");

            Optional<PeriodEntity> periodExist = periodRepository.findById(data.getPeriodId());
            if (periodExist.isEmpty())
                throw new BadRequestException("No existe el período");

            Optional<TaxTypeEntity> taxTypeExist = taxTypeRepository.findById(data.getTaxTypeId());
            if (taxTypeExist.isEmpty())
                throw new BadRequestException("No existe el tipo de tasa");
        }

        /*
        todo: hacer metodo para consulatr info de persona por su documento devolver informacion para llenar en el form
         */


    }
