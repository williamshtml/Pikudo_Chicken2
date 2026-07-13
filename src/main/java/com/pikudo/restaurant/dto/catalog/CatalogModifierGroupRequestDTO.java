package com.pikudo.restaurant.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogModifierGroupRequestDTO(
        @NotBlank(message = "El nombre del grupo es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        @Size(max = 150, message = "El slug no puede superar los 150 caracteres")
        String slug,
        @Size(max = 300, message = "La descripcion no puede superar los 300 caracteres")
        String description,
        @Min(value = 0, message = "minSelection no puede ser negativo")
        Integer minSelection,
        @Min(value = 0, message = "maxSelection no puede ser negativo")
        Integer maxSelection,
        Boolean required,
        Boolean active,
        Boolean publicVisible,
        Integer sortOrder
) {
}
