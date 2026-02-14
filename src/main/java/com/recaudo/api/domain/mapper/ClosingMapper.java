package com.recaudo.api.domain.mapper;

import com.recaudo.api.domain.model.dto.response.ClosingResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.ClosingDto;
import com.recaudo.api.domain.model.entity.ClosingEntity;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClosingMapper {

    ClosingResponseDto entityToDto(ClosingEntity entity);
    ClosingEntity dtoToEntity(ClosingDto entity);
    List<ClosingResponseDto> entitiesToDto(List<ClosingEntity> entity);

    default Date map(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return java.sql.Date.valueOf(LocalDate.parse(value));
    }

    default String map(Date value) {
        if (value == null) {
            return null;
        }
        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString();
    }

}
