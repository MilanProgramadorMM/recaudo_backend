package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CardDataDTO {
    private Long creditId;
    private String clientName;
    private Integer clientOrden;
    private String zonaCode;
    private String zona;
    private BigDecimal totalCapitalValue;
    private BigDecimal saldoPendiente;
    private BigDecimal totalMoraCredito;
    private Integer periodosVencidos;

    // Datos generales del crédito (antes solo en cobroHoy)
    private LocalDate fechaCredito;
    private String lineaname;
    private String periodo;
    private Integer plazoCredito;
    private LocalDate fechaVence;
    private Integer totalCuotas;
    private Integer cuotasPagadas;
    private Integer cuotasVencidas;
    private String direccion;
    private String whatsapp;
    private String celular;
    private String barrio;
    private String municipio;

    // Cuota específica (solo cobroHoy)
    private Long cuotaId;
    private Integer quotaNumber;
    private LocalDate expirationDate;
    private BigDecimal valorCuota;
    private BigDecimal saldoPendienteCuota;
    private BigDecimal interestMora;
    private Integer paidToday;
    private String paidFull;
    private String liquidated;
    private LocalDate paymentPromiseDate;
    private Integer noPago;
    private String noPagoReason;
    private String nombreDia;

    // Cartera
    private Integer cuotasPendientes;
    private LocalDate proximaCuotaFecha;
    private Integer proximaCuotaNumero;

    // Mora
    private LocalDate primeraCuotaVencida;
    private Integer primeraCuotaVencidaNumero;
}