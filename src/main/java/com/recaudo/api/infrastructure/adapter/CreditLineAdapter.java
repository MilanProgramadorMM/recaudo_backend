package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.CreditLineGateway;
import com.recaudo.api.domain.mapper.CreditLineMapper;
import com.recaudo.api.domain.model.dto.response.CreditLineResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditLineCreateDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.domain.model.entity.CreditLineEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.AmortizationTypeRepository;
import com.recaudo.api.infrastructure.repository.CreditLineDocumentationTypeRepository;
import com.recaudo.api.infrastructure.repository.CreditLineRepository;
import com.recaudo.api.infrastructure.repository.TaxTypeRepository;
import jakarta.transaction.Transactional;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreditLineAdapter implements CreditLineGateway {

    @Autowired
    CreditLineRepository creditLineRepository;
    @Autowired
    AmortizationTypeRepository amortizationTypeRepository;
    @Autowired
    TaxTypeRepository taxTypeRepository;

    @Autowired
    CreditLineDocumentationTypeRepository creditLineDocumentationTypeRepository;

    @Autowired(required = false)
    CreditLineMapper creditLineMapper = Mappers.getMapper(CreditLineMapper.class);

    @Override
    public List<CreditLineResponseDto> get() {
        List<CreditLineEntity> entities = creditLineRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return entities.stream()
                .map(entity -> {
                    CreditLineResponseDto dto = creditLineMapper.entityToDto(entity);

                    taxTypeRepository.findById(entity.getTaxType())
                            .ifPresent(taxType -> dto.setTaxTypeName(taxType.getName()));

                    amortizationTypeRepository.findById(entity.getAmortizationType())
                            .ifPresent(amortization -> dto.setAmortizationTypeName(amortization.getName()));

                    return dto;
                })
                .toList();
    }


    @Override
    public CreditLineResponseDto create(CreditLineCreateDto dto) {

        AmortizationTypeEntity amortizationType = amortizationTypeRepository.findById(dto.getAmortizationType())
                .orElseThrow(() -> new BadRequestException("Tipo de amortización no encontrado con id: "));

        TaxTypeEntity taxType = taxTypeRepository.findById(dto.getTaxType())
                .orElseThrow(() -> new BadRequestException("Tipo de impuesto no encontrado con id: " + dto.getTaxType()));

        validateCreditLineValues(dto);

        CreditLineEntity entity = CreditLineEntity.builder()
                .name(dto.getName().toUpperCase())
                .description(dto.getDescription().toUpperCase())
                .minQuota(dto.getMinQuota())
                .maxQuota(dto.getMaxQuota())
                .minPeriod(dto.getMinPeriod())
                .maxPeriod(dto.getMaxPeriod())
                .procedureName(dto.getProcedureName().toUpperCase())
                .lifeInsurance(dto.isLifeInsurance())
                .portfolioInsurance(dto.isPortfolioInsurance())
                .requireDocumentation(dto.isRequireDocumentation())
                .amortizationType(amortizationType.getId())
                .taxType(taxType.getId())
                .createdAt(LocalDateTime.now())
                .userCreate(getUsernameToken())
                .build();

        CreditLineEntity saved = creditLineRepository.save(entity);
        //Guardar documentos requeridos si aplica
        /*if (dto.isRequireDocumentation() && dto.getDocumentationTypeIds() != null) {
            List<CreditLineDocumentationTypeEntity> docs = dto.getDocumentationTypeIds().stream()
                    .map(docId -> CreditLineDocumentationTypeEntity.builder()
                            .creditLineId(saved.getId())
                            .documentationTypeId(docId)
                            .createdAt(LocalDateTime.now())
                            .userCreate(getUsernameToken())
                            .build())
                    .toList();

            creditLineDocumentationTypeRepository.saveAll(docs);
        }*/

        return creditLineMapper.entityToDto(saved);
    }

    //TENER EN CUENTA QUE PARA EDITAR UNA LINEA ANTES SE DEBE CONSULTAR SI ESTA NO TIENE ALGUN CREDITO ASOCIADO
    // YA QUE PODRIA CAUSAR CAOS Y ALTERAR EL FLUJO, TEMA DELICADO
    @Override
    @Transactional
    public CreditLineResponseDto update(Long id, CreditLineCreateDto dto) {
        CreditLineEntity entity = creditLineRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La línea de crédito con id " + id + " no existe"));

        validateCreditLineValues(dto);

        AmortizationTypeEntity amortizationType = amortizationTypeRepository.findById(dto.getAmortizationType())
                .orElseThrow(() -> new BadRequestException("Tipo de amortización no encontrado con id: " + dto.getAmortizationType()));

        TaxTypeEntity taxType = taxTypeRepository.findById(dto.getTaxType())
                .orElseThrow(() -> new BadRequestException("Tipo de impuesto no encontrado con id: " + dto.getTaxType()));

        entity.setName(dto.getName().toUpperCase());
        entity.setDescription(dto.getDescription().toUpperCase());
        entity.setMinQuota(dto.getMinQuota());
        entity.setMaxQuota(dto.getMaxQuota());
        entity.setMinPeriod(dto.getMinPeriod());
        entity.setMaxPeriod(dto.getMaxPeriod());
        entity.setProcedureName(dto.getProcedureName().toUpperCase());
        entity.setLifeInsurance(dto.isLifeInsurance());
        entity.setPortfolioInsurance(dto.isPortfolioInsurance());
        entity.setRequireDocumentation(dto.isRequireDocumentation());
        entity.setAmortizationType(amortizationType.getId());
        entity.setTaxType(taxType.getId());
        entity.setEditedAt(LocalDateTime.now());
        entity.setUserEdit(getUsernameToken());

        CreditLineEntity updated = creditLineRepository.save(entity);

        return creditLineMapper.entityToDto(updated);
    }

    //TENER EN CUENTA QUE PARA ELIMINAR UNA LINEA ANTES SE DEBE CONSULTAR SI ESTA NO TIENE ALGUN CREDITO ASOCIADO, TEMA DELICADO
    @Override
    public CreditLineResponseDto delete(Long id) {
        CreditLineEntity creditLineEntity = creditLineRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("linea de credito no encontrada con id " + id));

        creditLineEntity.setStatus(false);
        creditLineEntity.setDeletedAt(LocalDateTime.now());
        creditLineEntity.setUserDelete(getUsernameToken());
        creditLineRepository.save(creditLineEntity);

        return creditLineMapper.entityToDto(creditLineEntity);
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    private void validateCreditLineValues(CreditLineCreateDto dto) {
        if (dto.getMaxQuota() != null && dto.getMinQuota() != null &&
                dto.getMaxQuota().compareTo(dto.getMinQuota()) <= 0) {
            throw new BadRequestException("La cuota máxima debe ser mayor que la mínima");
        }

        if (dto.getMaxPeriod() != null && dto.getMinPeriod() != null &&
                dto.getMaxPeriod() <= dto.getMinPeriod()) {
            throw new BadRequestException("El período máximo debe ser mayor que el mínimo");
        }
    }

}
