package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.ClosingStatusResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ChangeClosingStatusDto;
import com.recaudo.api.domain.model.entity.ClosingStatusEntity;
import com.recaudo.api.infrastructure.helper.util.ClosingStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClosingStatusMapper {

    @Mapping(target = "code", source = "closingStatus")
    @Mapping(target = "startDate", expression = "java(entity.getStartDate() != null ? entity.getStartDate().toString() : null)")
    @Mapping(target = "endDate", expression = "java(entity.getEndDate() != null ? entity.getEndDate().toString() : null)")
    ClosingStatusResponseDto entityToDto(ClosingStatusEntity entity);
    ClosingStatusEntity dtoToEntity(ChangeClosingStatusDto entity);
    List<ClosingStatusResponseDto> entitiesToDto(List<ClosingStatusEntity> entity);
    List<ClosingStatusEntity> dtoToEntities(List<ClosingStatusResponseDto> entity);

    default String map(ClosingStatus status) {
        return status != null ? status.name() : null;
    }
}
