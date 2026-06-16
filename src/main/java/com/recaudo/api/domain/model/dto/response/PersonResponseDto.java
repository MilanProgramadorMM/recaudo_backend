package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
public class PersonResponseDto {
    private Long id;
    private Integer documentType;
    private String document;
    private String firstName;
    private String middleName;
    private String lastName;
    private String maternalLastname;
    private String fullName;
    private Integer gender;
    private String occupation;
    private String description;
    private String createdAt;
    private String typePerson;
    private Boolean status;
    private String uniqueCode;

    // Para CLIENTE (una sola zona con orden)
    private Integer orden;
    private Long zonaId;
    private String zona;

    // Para ASESOR (múltiples zonas sin orden)
    private List<AsesorZonaDto> zonas;

    // Campos de ubicación
    private Long countryId;
    private Long departentId;
    private Long cityId;
    private Long neighborhoodId;
    private String descriptionD;
    private String adress;
    private String details;
    private String correo;
    private String celular;
    private String whatsApp;

    // Información de crédito
    private Long creditId;
    private Double creditAmount;
    private Double creditBalance;
    private String creditStatus;

    // Información de cierre del día
    private Boolean hasClosingToday;
    private String closingStatus;
}
