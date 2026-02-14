package com.recaudo.api.domain.model.dto.rest_api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementData {

    private Long id;
    private Long paymentTypeId;
    private Long bankId;
    private String accountNumber;
    private Double amount;
    private Boolean hasFile;
    private Integer fileIndex;  // Índice del archivo en la lista de MultipartFile
}