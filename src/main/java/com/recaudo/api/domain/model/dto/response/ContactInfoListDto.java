package com.recaudo.api.domain.model.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class ContactInfoListDto {
    private Long id;
    private String typeCode;
    private String typeName;
    private String value;
    private String country;
    private String department;
    private String city;
    private String neighborhood;
    private String description;
}
