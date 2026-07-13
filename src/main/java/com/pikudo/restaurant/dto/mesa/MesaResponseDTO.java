package com.pikudo.restaurant.dto.mesa;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos de lectura para que el frontend dibuje las mesas en el salón
@Setter              // Genera los métodos de escritura para mapear la entidad de la base de datos
@NoArgsConstructor   // Constructor vacío () estándar para Jackson
@AllArgsConstructor  // Constructor completo optimizado en una sola línea por Lombok
public class MesaResponseDTO {

    private Long id;           // ID único de la mesa en la base de datos
    private Integer numero;    // Número físico de la mesa (ej: 1, 2, 3)
    private Integer capacidad; // Cantidad máxima de personas que entran en la mesa
    private String estado;     // Estado actual de la mesa (ej: "DISPONIBLE", "OCUPADA", "RESERVADA")
}
