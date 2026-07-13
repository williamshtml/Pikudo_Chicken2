package com.pikudo.dto.comprobante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Valid
    private List<PagoDetalleDTO> pagos;

    private String ruc;
    private String razonSocial;
    private String tipoDocumentoCliente;
    private String numeroDocumentoCliente;
    private String direccionCliente;
}
