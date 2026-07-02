package com.pikudo.dto.caja;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaDTO {

    private Long id;
    private String usuarioUsername; // Nombre del cajero responsable

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    @NotNull(message = "El monto inicial de apertura es obligatorio")
    @PositiveOrZero(message = "El monto inicial no puede ser negativo")
    private BigDecimal montoInicial;

    // Totales calculados automáticamente por el sistema
    private BigDecimal montoVentasEfectivo;
    private BigDecimal montoVentasTarjeta;
    private BigDecimal montoVentasDigital;
    private BigDecimal montoGastos;
    private BigDecimal montoFinalSistema;

    // Declaración física del cajero al cerrar
    @PositiveOrZero(message = "El monto final real no puede ser negativo")
    private BigDecimal montoFinalReal;

    private String observaciones;
    private String estado; // "ABIERTA" o "CERRADA"
}