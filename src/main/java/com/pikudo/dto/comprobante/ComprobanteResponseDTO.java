/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.comprobante;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida para devolver el comprobante generado al cliente.
 * Agregado por: [tu nombre] - Módulo de comprobantes/facturación.
 */

public class ComprobanteResponseDTO {

    private Long id;
    private Long pedidoId;
    private String tipoComprobante;
    private String serie;
    private Integer numeroCorrelativo;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String ruc;
    private String razonSocial;
    private LocalDateTime fechaEmision;

    public ComprobanteResponseDTO() {
    }

    public ComprobanteResponseDTO(Long id, Long pedidoId, String tipoComprobante, String serie, Integer numeroCorrelativo,
                                   String metodoPago, BigDecimal subtotal, BigDecimal igv, BigDecimal total,
                                   String ruc, String razonSocial, LocalDateTime fechaEmision) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.tipoComprobante = tipoComprobante;
        this.serie = serie;
        this.numeroCorrelativo = numeroCorrelativo;
        this.metodoPago = metodoPago;
        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.fechaEmision = fechaEmision;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public Integer getNumeroCorrelativo() {
        return numeroCorrelativo;
    }

    public void setNumeroCorrelativo(Integer numeroCorrelativo) {
        this.numeroCorrelativo = numeroCorrelativo;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
