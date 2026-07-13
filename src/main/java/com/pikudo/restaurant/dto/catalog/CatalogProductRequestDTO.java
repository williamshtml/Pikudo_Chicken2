package com.pikudo.restaurant.dto.catalog;

import com.pikudo.restaurant.entity.ProductoTipo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CatalogProductRequestDTO(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        @Size(max = 160, message = "El slug no puede superar los 160 caracteres")
        String slug,
        @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres")
        String description,
        @NotNull(message = "La categoria es obligatoria")
        Long categoryId,
        ProductoTipo type,
        @NotNull(message = "El precio base es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
        BigDecimal basePrice,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,
        Integer sortOrder,
        Boolean publicVisible,
        Boolean available,
        Boolean active,
        @Size(max = 120, message = "El nombre de variante no puede superar los 120 caracteres")
        String initialVariantName,
        @Size(max = 80, message = "El SKU no puede superar los 80 caracteres")
        String sku
) {
}
