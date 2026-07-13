package com.pikudo.restaurant.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CatalogProductVariantRequestDTO(
        @NotBlank(message = "El nombre de la variante es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        @Size(max = 80, message = "El SKU no puede superar los 80 caracteres")
        String sku,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
        BigDecimal price,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,
        Integer sortOrder,
        Boolean publicVisible,
        Boolean available
) {
}
