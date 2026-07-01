package com.pikudo.dto.comprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos de lectura para pintar el comprobante en la UI
@Setter              // Genera los métodos de escritura para armar la respuesta desde el Service
@NoArgsConstructor   // Constructor vacío () estándar para Jackson
@AllArgsConstructor  // Constructor gigante resuelto por Lombok en una sola línea
public class ComprobanteResponseDTO {

    private Long id;              // ID único del comprobante emitido
    private Long pedidoId;        // ID del pedido de origen
    private String tipoComprobante;// BOLETA, FACTURA o TICKET
    private String serie;         // Serie del documento (ej: "F001", "B001")
    private Integer numeroCorrelativo; // Número secuencial autoincrementable (ej: 145)
    private String metodoPago;    // EFECTIVO, TARJETA, YAPE, PLIN
    private BigDecimal subtotal;  // Monto base neto sin impuestos
    private BigDecimal igv;       // Impuesto calculado (18%)
    private BigDecimal total;     // Monto final cobrado al cliente de la pollería
    private String ruc;           // RUC de la empresa (si fue factura)
    private String razonSocial;   // Nombre legal de la empresa (si fue factura)
    private LocalDateTime fechaEmision; // Fecha y hora exacta en la que se cerró la venta
    
    // Campos incorporados para impresión en ticket
    private String nombreCajero;  // Nombre del usuario que procesó el cobro
    private String nombreMesero;  // Nombre del usuario que atendió la mesa (nulo si es pedido de caja)
}