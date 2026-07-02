package com.pikudo.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

public class ReporteDTO {

    @Getter @Setter @AllArgsConstructor
    public static class UtilidadNetaDTO {
        private BigDecimal totalVentas;
        private BigDecimal totalGastos;
        private BigDecimal utilidadNeta;
    }

    @Getter @Setter @AllArgsConstructor
    public static class ProductoMasVendidoDTO {
        private String productoNombre;
        private Long cantidadVendida;
        private BigDecimal totalRecaudado;
    }

    @Getter @Setter @AllArgsConstructor
    public static class FlujoHorarioDTO {
        private Integer hora; // Ej: 13 (1:00 PM), 20 (8:00 PM)
        private Long cantidadPedidos;
    }
}