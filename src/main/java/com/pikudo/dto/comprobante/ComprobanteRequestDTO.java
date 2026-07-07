package com.pikudo.dto.comprobante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRequestDTO {
    @NotNull(message = "El pedido es obligatorio")
    private Long pedidoId;          // ID del pedido que se va a cerrar y cobrar
    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante; // Puede recibir BOLETA, FACTURA o TICKET

    // Lista de pagos: normalmente 1 elemento, pero soporta pagos divididos
    // (ej: mitad efectivo, mitad Yape). La suma debe coincidir con el total del pedido.
    @NotEmpty(message = "Debe especificar al menos un método de pago")
    @Valid
    private List<PagoDetalleDTO> pagos;

    private String ruc;             // RUC de la empresa (obligatorio solo si tipoComprobante = FACTURA)
    private String razonSocial;     // Nombre de la empresa (obligatorio solo si tipoComprobante = FACTURA)
}