package com.pikudo.restaurant.dto.inventario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDTO {

    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;
    private String productoNombre; // Útil para mostrar en listados del frontend

    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;
    private String insumoNombre;   // Útil para el frontend (ej: "Papa Amarilla")
    private String unidadMedida;   // Para saber en qué se mide (ej: "KG")

    @NotNull(message = "La cantidad o porción es obligatoria")
    @Positive(message = "La porción de la receta debe ser mayor a cero")
    private BigDecimal cantidad; // Ej: 0.400 (400 gramos de papa)
}