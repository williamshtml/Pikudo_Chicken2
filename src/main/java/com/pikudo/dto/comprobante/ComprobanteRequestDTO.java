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
    private Long pedidoId;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    @NotEmpty(message = "Debe especificar al menos un método de pago")
    @Valid
    private List<PagoDetalleDTO> pagos;

    private String ruc;
    private String razonSocial;

    private String tipoDocumentoCliente;
    private String numeroDocumentoCliente;
    private String direccionCliente;
}