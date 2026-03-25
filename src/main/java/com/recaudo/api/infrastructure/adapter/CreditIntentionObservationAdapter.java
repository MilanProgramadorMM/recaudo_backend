package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.CreditIntentionObservationGateway;
import com.recaudo.api.domain.mapper.CreditIntentionObservationMapper;
import com.recaudo.api.domain.model.dto.response.CreditIntentionObservationResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeCreditStatusDto;
import com.recaudo.api.domain.model.entity.ObservationCreditIntentionEntity;
import com.recaudo.api.infrastructure.repository.CreditIntentionObservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CreditIntentionObservationAdapter implements CreditIntentionObservationGateway {

    @Autowired
    private CreditIntentionObservationRepository creditIntentionObservationRepository;


    @Autowired(required = false)
    CreditIntentionObservationMapper creditIntentionObservationMapper = Mappers.getMapper(CreditIntentionObservationMapper.class);


    @Override
    public CreditIntentionObservationResponseDto create(Long creditIntentionId,
                                                        String observation,
                                                        String activity,
                                                        String statusStart,
                                                        String statusEnd ) {
        ObservationCreditIntentionEntity entity = new ObservationCreditIntentionEntity();

        entity.setCreditIntentionId(creditIntentionId);
        entity.setObservation(observation.toUpperCase());
        entity.setActivity(activity.toUpperCase());
        entity.setCreditIntentionStatusStart(statusStart);
        entity.setCreditIntentionStatusEnd(statusEnd);
        entity.setUserCreate(getUsernameToken());
        entity.setCreatedAt(LocalDateTime.now());

        ObservationCreditIntentionEntity entitySaved =  creditIntentionObservationRepository.save(entity);

        return creditIntentionObservationMapper.entityToDto(entitySaved);
    }

    @Override
    public CreditIntentionObservationResponseDto createIndividual(ChangeCreditStatusDto dto) {
        ObservationCreditIntentionEntity entity = new ObservationCreditIntentionEntity();
        entity.setCreditIntentionId(dto.getCreditId());
        entity.setObservation(dto.getObservation());
        entity.setActivity(dto.getActivity());
        entity.setCreditIntentionStatusStart(dto.getNewStatus().toString());
        entity.setCreditIntentionStatusEnd(dto.getNewStatus().toString());
        entity.setUserCreate(getUsernameToken());
        entity.setCreatedAt(LocalDateTime.now());

        ObservationCreditIntentionEntity saved = creditIntentionObservationRepository.save(entity);
        return creditIntentionObservationMapper.entityToDto(saved);
    }

    @Override
    public List<CreditIntentionObservationResponseDto> findByCreditIntentionId(Long creditIntentionId) {
        return creditIntentionObservationRepository
                .findByCreditIntentionIdOrderByCreatedAtAsc(creditIntentionId)
                .stream()
                .map(creditIntentionObservationMapper::entityToDto)
                .collect(Collectors.toList());
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

}
