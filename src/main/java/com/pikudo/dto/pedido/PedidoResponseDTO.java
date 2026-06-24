/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para devolver datos completos de un Pedido al cliente,
 * incluyendo la lista de productos pedidos (detalles).
 * La clase interna DetalleItemDTO representa cada línea/producto del pedido ya registrado.
 * Agregado por: [tu nombre] - Módulo de pedidos.
 */

public class PedidoResponseDTO {

    private Long id;
    private Integer mesaNumero;
    private String usuarioNombre;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private String estadoPedido;
    private List<DetalleItemDTO> detalles;

    public PedidoResponseDTO() {
    }

    public PedidoResponseDTO(Long id, Integer mesaNumero, String usuarioNombre, LocalDateTime fechaHora,
                              BigDecimal total, String estadoPedido, List<DetalleItemDTO> detalles) {
        this.id = id;
        this.mesaNumero = mesaNumero;
        this.usuarioNombre = usuarioNombre;
        this.fechaHora = fechaHora;
        this.total = total;
        this.estadoPedido = estadoPedido;
        this.detalles = detalles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMesaNumero() {
        return mesaNumero;
    }

    public void setMesaNumero(Integer mesaNumero) {
        this.mesaNumero = mesaNumero;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public List<DetalleItemDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleItemDTO> detalles) {
        this.detalles = detalles;
    }

    /**
     * Clase interna que representa una línea de producto dentro del pedido ya registrado,
     * con el precio capturado, subtotal y observaciones de cocina.
     */
    public static class DetalleItemDTO {

        private Long id;
        private String productoNombre;
        private BigDecimal precioUnitario;
        private Integer cantidad;
        private BigDecimal subtotal;
        private String observaciones;

        public DetalleItemDTO() {
        }

        public DetalleItemDTO(Long id, String productoNombre, BigDecimal precioUnitario, Integer cantidad,
                               BigDecimal subtotal, String observaciones) {
            this.id = id;
            this.productoNombre = productoNombre;
            this.precioUnitario = precioUnitario;
            this.cantidad = cantidad;
            this.subtotal = subtotal;
            this.observaciones = observaciones;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProductoNombre() {
            return productoNombre;
        }

        public void setProductoNombre(String productoNombre) {
            this.productoNombre = productoNombre;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
    }
}
