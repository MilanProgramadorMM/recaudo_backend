package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
public class PersonResponseDto {

    private Long id;
    private Long documentType;
    private String document;
    private String firstName;
    private String middleName;
    private String lastName;
    private String maternalLastname;
    private String fullName;
    private Long gender;
    private String occupation;
    private String description;
    private Long orden;
    private String zona;
    private String countryId;
    private String cityId;
    private String departentId;
    private String neighborhoodId;
    private String adress;
    private String correo;
    private String celular;
    private String telefono;
}
