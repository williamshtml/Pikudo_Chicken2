package com.pikudo.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Métodos de lectura automáticos para la cabecera del pedido
@Setter              // Métodos de escritura automáticos para el backend
@NoArgsConstructor   // Constructor vacío () obligatorio para Jackson
@AllArgsConstructor  // Constructor lleno útil para pruebas del sistema
public class PedidoRequestDTO {

    @NotNull(message = "La mesa es obligatoria")
    private Long mesaId;           // ID de la mesa asignada en el salón

    @NotNull(message = "El usuario que registra el pedido es obligatorio")
    private Long usuarioId;        // ID del mozo o cajero que abre la comanda

    @Valid                 // Obliga a validar las restricciones de cada ítem dentro de la lista
    private List<DetalleItemDTO> detalles; // Listado de platos y bebidas pedidos

    @Getter              // Getters para cada línea de la comanda
    @Setter              // Setters para cada línea de la comanda
    @NoArgsConstructor   // Constructor vacío () para mapear los elementos de la lista
    @AllArgsConstructor  // Constructor completo para añadir líneas rápidamente
    public static class DetalleItemDTO {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;    // ID del plato o bebida (ej: 1/4 de pollo)

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;   // Cuántas unidades se solicitan de ese producto

        @Size(max = 200, message = "Las observaciones no pueden superar los 200 caracteres")
        private String observaciones; // Notas especiales de cocina (ej: "Sin ensalada", "Parte pechuga")
    }
}