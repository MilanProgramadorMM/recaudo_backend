package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshotResponseDto {

    private Long id;
    private LocalDate snapshotDate;
    private Integer creditId;
    private Integer personId;
    private String clienteFullname;
    private String clienteDocumento;

    private Long zonaId;
    private String zonaNombre;
    private String estadoCredito;

    private Long creditLineId;
    private String creditLineNombre;
    private Long periodId;
    private String periodNombre;
    private String periodCodigo;
    private Long taxTypeId;
    private String taxTypeNombre;
    private BigDecimal taxValue;

    private Integer cuotasPlaneadas;
    private Integer totalCuotas;
    private Integer cuotasPagadas;
    private Integer cuotasPendientes;

    private BigDecimal capitalGenerado;
    private BigDecimal capitalPagado;
    private BigDecimal capitalPendiente;

    private BigDecimal interesGenerado;
    private BigDecimal interesPagado;
    private BigDecimal interesPendiente;

    private BigDecimal seguroVidaGenerado;
    private BigDecimal seguroVidaPagado;
    private BigDecimal seguroVidaPendiente;

    private BigDecimal seguroCarteraGenerado;
    private BigDecimal seguroCarteraPagado;
    private BigDecimal seguroCarteraPendiente;

    private BigDecimal moraGenerada;
    private BigDecimal moraPagada;
    private BigDecimal moraPendiente;
    private Integer diasMora;

    private BigDecimal totalPagado;
    private BigDecimal saldoTotal;
    private BigDecimal otrosConceptosGenerado;

    private String ratingValue;
    private Integer ratingRangeStart;
    private Integer ratingRangeEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String jobExecutionId;
}
