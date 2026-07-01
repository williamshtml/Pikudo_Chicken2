package com.pikudo.dto.pedido;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PedidoResponseDTO {

    private Long id;
    private Integer mesaNumero;
    
    // --- CAMPOS DE TRAZABILIDAD DE ROLES ---
    private String cajeroNombre;      // Quién cobró
    private String responsableNombre; // Quién atendió (Sheyla, Rolando, etc.)
    private String responsableRol;    // "Mesero", "Repartidor" o "Venta Directa"
    private String usuarioNombre; // <--- AGREGA ESTO de vuelta para que desaparezcan los errores en tu mapper
    
    // ─── NUEVOS CAMPOS PARA EL REPARTIDOR ───────────────────────────────────
    private String direccion;
    private String urlMaps;
    
// ...
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
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