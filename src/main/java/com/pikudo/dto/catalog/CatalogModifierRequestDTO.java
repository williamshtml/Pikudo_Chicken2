package com.pikudo.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CatalogModifierRequestDTO(
        @NotBlank(message = "El nombre del modificador es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        @Size(max = 150, message = "El slug no puede superar los 150 caracteres")
        String slug,
        @DecimalMin(value = "0.0", message = "El precio extra no puede ser negativo")
        BigDecimal extraPrice,
        Boolean active,
        Boolean publicVisible,
        Integer sortOrder
) {
}
