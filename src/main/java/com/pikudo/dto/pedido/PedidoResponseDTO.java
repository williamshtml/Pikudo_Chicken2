package com.pikudo.dto.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private Integer mesaNumero;
    private String usuarioNombre;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private BigDecimal subtotalNeto;
    private BigDecimal igv;
    private String estadoPedido;
    private List<DetalleItemDTO> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleItemDTO {
        private Long id;
        private String productoNombre;
        private BigDecimal precioUnitario;
        private Integer cantidad;
        private BigDecimal subtotal;
        private String observaciones;
    }
}