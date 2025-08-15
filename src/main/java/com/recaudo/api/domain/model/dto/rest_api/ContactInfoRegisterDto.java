package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class ContactInfoRegisterDto {
    private Long personId;
    private Long typeId;
    private String value;
    private Long city;
    private Long department;
    private Long country;
    private Long neighborhood;
    private String description;
}
