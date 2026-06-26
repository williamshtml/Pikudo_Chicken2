package com.pikudo.dto.comprobante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera métodos para leer el pedido, método de pago, etc.
@Setter              // Genera métodos para asignar valores en la lógica del backend
@NoArgsConstructor   // Constructor vacío () obligatorio para que Spring capture el JSON
@AllArgsConstructor  // Constructor completo útil para pruebas de facturación
public class ComprobanteRequestDTO {

    @NotNull(message = "El pedido es obligatorio")
    private Long pedidoId;          // ID del pedido que se va a cerrar y cobrar

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante; // Puede recibir BOLETA, FACTURA o TICKET

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;      // Soporta los métodos locales: EFECTIVO, TARJETA, YAPE, PLIN

    private String ruc;             // RUC de la empresa (obligatorio solo si tipoComprobante = FACTURA)

    private String razonSocial;     // Nombre de la empresa (obligatorio solo si tipoComprobante = FACTURA)
}