package com.pikudo.restaurant.dto.mesa;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MesaResponseDTO {
    private Long id;           // ID único de la mesa en la base de datos
    private Integer numero;    // Número físico de la mesa (ej: 1, 2, 3)
    private Integer capacidad; // Cantidad máxima de personas que entran en la mesa
    private Integer salon;     // Salón/piso al que pertenece la mesa (1, 2, ...)
    private String estado;     // "DISPONIBLE" | "INACTIVA"
}