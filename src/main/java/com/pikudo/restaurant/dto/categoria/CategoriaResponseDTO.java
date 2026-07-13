package com.pikudo.restaurant.dto.categoria;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para que el frontend pueda leer el id, nombre y descripción
@Setter              // Genera los métodos para asignar los datos que vienen de la base de datos
@NoArgsConstructor   // Constructor vacío () que usa Jackson para construir la respuesta JSON
@AllArgsConstructor  // Constructor completo para mapear la Entity a este DTO en una sola línea
public class CategoriaResponseDTO {

    private Long id;             // ID único de la categoría (básico para que el frontend filtre o edite)
    private String nombre;       // Nombre de la categoría (ej: "Pollos", "Bebidas") que verá el cliente
    private String descripcion;  // Detalle opcional de la categoría mapeado desde la base de datos
}
