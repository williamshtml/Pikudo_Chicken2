package com.pikudo.restaurant.dto.mesa;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos de lectura automáticamente
@Setter              // Genera los métodos de escritura automáticamente
@NoArgsConstructor   // Constructor vacío () estándar para mapear el JSON entrante
@AllArgsConstructor  // Constructor completo para instanciar rápido en la lógica del Service
public class MesaRequestDTO {

    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número de mesa debe ser mayor a 0")
    private Integer numero;    // El número físico que tiene la mesa en la pollería (ej: Mesa 1, Mesa 2)

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1 persona")
    private Integer capacidad; // Cantidad máxima de comensales permitidos en esa mesa
}
