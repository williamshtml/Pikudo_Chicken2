package com.pikudo.restaurant.dto.mesa;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Extiende la info de la mesa con su estado de OCUPACION calculado en tiempo real
 * (si tiene un pedido activo ahora mismo), distinto de si la mesa esta activa
 * en el catalogo (Mesa.estado). Ver MesaServiceImpl.listarConOcupacion().
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MesaEstadoResponseDTO {
    private Long id;
    private Integer numero;
    private Integer capacidad;
    private boolean activaEnCatalogo; // false = fue dada de baja, ya no existe como mesa fisica
    private boolean ocupada;          // true = tiene un pedido abierto ahora mismo
}