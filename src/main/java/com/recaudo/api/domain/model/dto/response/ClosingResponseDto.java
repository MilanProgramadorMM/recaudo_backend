package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClosingResponseDto {

    private Long id;
    private Double amount;
    private String closingDate;
    private Long personId;
    private String namePerson;
    private Double amountAdmin;
    private Double amountAsesor;
    private String deliveryType;
    private String closingStatus;
    private String observation;
    private String userCreate;
    private String userEdit;
    private String createdAt;
    private String editedAt;
    private Boolean status;
    private String zona;
    private Long zonaId;



}
