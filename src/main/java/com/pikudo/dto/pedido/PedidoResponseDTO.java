package com.pikudo.dto.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera métodos de lectura para la cabecera del pedido
@Setter              // Genera métodos de escritura para armar la respuesta desde el Service
@NoArgsConstructor   // Constructor vacío () estándar para Jackson
@AllArgsConstructor  // Constructor completo para mapear la entidad padre de golpe
public class PedidoResponseDTO {

    private Long id;              // ID único del pedido generado en el sistema
    private Integer mesaNumero;   // Número de la mesa (ej: 4) en lugar de mandar todo el objeto Mesa
    private String usuarioNombre; // Nombre del mozo/cajero que atiende el pedido
    private LocalDateTime fechaHora; // Fecha y hora exacta en la que se abrió la comanda
    private BigDecimal total;     // Monto acumulado total calculado por el backend
    private String estadoPedido;  // Estado actual del flujo (ej: "PENDIENTE", "PREPARADO", "PAGADO")
    private List<DetalleItemDTO> detalles; // El desglose de los platos pedidos

    @Getter              // Getters automáticos para cada línea de la respuesta
    @Setter              // Setters automáticos para armar el listado
    @NoArgsConstructor   // Constructor vacío () requerido para la lista interna
    @AllArgsConstructor  // Constructor completo para instanciar las líneas en el Service
    public static class DetalleItemDTO {

        private Long id;              // ID único de la línea de detalle
        private String productoNombre;// Nombre del plato o bebida (ej: "1/2 Pollo a la Brasa")
        private BigDecimal precioUnitario; // Precio del producto congelado al momento de la compra
        private Integer cantidad;     // Unidades solicitadas de este ítem
        private BigDecimal subtotal;  // Cálculo matemático exacto de precio * cantidad
        private String observaciones; // Notas de preparación enviadas a cocina
    }
}
