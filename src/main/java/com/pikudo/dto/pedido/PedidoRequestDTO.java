package com.pikudo.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {

    // Cambiamos a no obligatorio para permitir pedidos de Delivery sin mesa física
    private Long mesaId;

    // Ya no lo enviaremos obligatoriamente desde el frontend, 
    // porque el servidor lo obtendrá automáticamente de la sesión (SecurityContextHolder).
    // Si prefieres mantenerlo, está bien, pero el Servicio ya no dependerá de él.
    private Long usuarioId; 

    // Agregamos este campo esencial para saber cómo procesar el pedido en el mapper
    @NotNull(message = "El tipo de pedido es obligatorio (MESA, DELIVERY)")
    private String tipoPedido;

    @Valid
    @NotNull(message = "El pedido debe tener al menos un detalle")
    private List<DetalleItemDTO> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleItemDTO {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @Size(max = 200, message = "Las observaciones no pueden superar los 200 caracteres")
        private String observaciones;
    }
}