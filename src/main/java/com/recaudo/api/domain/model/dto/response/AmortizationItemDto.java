package com.recaudo.api.domain.model.dto.response;


import lombok.Data;

@Data
public class AmortizationItemDto {
    private int periodo;
    private double cuota;
    private double interes;
    private double amortizacion;
    private double saldo;


    public AmortizationItemDto(int periodo, double cuota, double interes, double amortizacion, double saldo) {
        this.periodo = periodo;
        this.cuota = cuota;
        this.interes = interes;
        this.amortizacion = amortizacion;
        this.saldo = saldo;
    }



}
