package com.pikudo.dto.caja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaResumenDTO {
    private Long cajaId;
    private BigDecimal montoInicial;
    private BigDecimal montoVentasEfectivo;
    private BigDecimal montoVentasTarjeta;
    private BigDecimal montoVentasDigital;
    private BigDecimal montoVentasTotal;
    private BigDecimal montoGastos;
    private BigDecimal montoEsperadoEnCajon; // montoInicial + efectivo - gastos
    private BigDecimal montoFinalReal;
    private BigDecimal diferencia;
    private Boolean tienePagosParcialesPendientes;
}
