package com.pikudo.dto.cateogira;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para leer el nombre y descripción desde el formulario
@Setter              // Genera los métodos para asignar los valores en el backend
@NoArgsConstructor   // Constructor vacío () que usa Jackson para mapear el JSON del cliente
@AllArgsConstructor  // Constructor completo útil para crear instancias rápidas en servicios
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;       // Ej: "Bebidas", "Pollos a la Brasa", "Guarniciones"

    @Size(max = 150, message = "La descripción no puede superar los 150 caracteres")
    private String descripcion;  // Breve detalle opcional de lo que incluye la categoría
}