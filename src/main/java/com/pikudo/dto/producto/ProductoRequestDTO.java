package com.pikudo.dto.producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para leer los datos enviados desde el formulario
@Setter              // Genera los métodos para asignarlos al procesar la petición
@NoArgsConstructor   // Constructor vacío () estándar para Jackson
@AllArgsConstructor  // Constructor lleno listo para usar en los servicios de la carta
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;       // Ej: "1/4 de Pollo a la Brasa", "Chicha Morada 1L"

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;   // Valor monetario exacto del plato

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;       // Cantidad disponible en cocina/barra

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;    // ID de la categoría a la que pertenece (vía combo box)
}
