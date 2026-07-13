package com.pikudo.restaurant.dto.comprobante;

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
public class ComprobanteResponseDTO {

    private Long id;
    private Long pedidoId;
    private String tipoComprobante;
    private String serie;
    private Integer numeroCorrelativo;
    private List<PagoDetalleDTO> pagos;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String ruc;
    private String razonSocial;
    private String tipoDocumentoCliente;
    private String numeroDocumentoCliente;
    private String clienteNombreSnapshot;
    private String estadoSunat;
    private String mensajeSunat;
    private String documentFolderType;
    private LocalDateTime fechaEmision;
    private String nombreCajero;
    private String nombreMesero;
}
