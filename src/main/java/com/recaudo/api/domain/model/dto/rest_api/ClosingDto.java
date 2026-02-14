package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClosingDto {

    private Double amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String closingDate;

    private Long personId;
    private Long zonaId;
    private String closingStatus;
    private String observation;
}
