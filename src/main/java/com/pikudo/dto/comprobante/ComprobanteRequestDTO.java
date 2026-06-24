/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.comprobante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para generar el comprobante de cierre de un Pedido.
 * El subtotal, IGV y total NO se reciben del cliente: se recalculan en backend
 * a partir del pedido real guardado en base de datos, para evitar fraude o manipulación.
 * El ruc y razonSocial solo son obligatorios si tipoComprobante es FACTURA (se valida en el service).
 * Agregado por: [tu nombre] - Módulo de comprobantes/facturación.
 */

public class ComprobanteRequestDTO {
    

    @NotNull(message = "El pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante; // BOLETA, FACTURA, TICKET

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago; // EFECTIVO, TARJETA, YAPE, PLIN

    private String ruc; // obligatorio solo si tipoComprobante = FACTURA

    private String razonSocial; // obligatorio solo si tipoComprobante = FACTURA

    public ComprobanteRequestDTO() {
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
}