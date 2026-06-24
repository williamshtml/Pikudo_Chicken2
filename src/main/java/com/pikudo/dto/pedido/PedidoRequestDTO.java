
package com.pikudo.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO de entrada para crear un nuevo Pedido.
 * Recibe la mesa, el usuario (mozo/cajero) que lo abre y la lista de detalles (productos pedidos).
 * El total se calcula en el service sumando los subtotales, nunca se recibe del cliente.
 * La clase interna DetalleItemDTO representa cada línea/producto del pedido,
 * ya que no existe un archivo DetallePedidoDTO independiente en el proyecto.
 * Agregado por: [tu nombre] - Módulo de pedidos.
 */

public class PedidoRequestDTO {



    @NotNull(message = "La mesa es obligatoria")
    private Long mesaId;

    @NotNull(message = "El usuario que registra el pedido es obligatorio")
    private Long usuarioId;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<DetalleItemDTO> detalles;

    public PedidoRequestDTO() {
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<DetalleItemDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleItemDTO> detalles) {
        this.detalles = detalles;
    }

    /**
     * Clase interna que representa una línea de producto dentro del pedido entrante.
     * El cliente solo manda producto, cantidad y observaciones; el precio y subtotal
     * los calcula el service en backend, NUNCA se confía en un precio enviado desde el frontend.
     */
    public static class DetalleItemDTO {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @Size(max = 200)
        private String observaciones;

        public DetalleItemDTO() {
        }

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
    }
}
