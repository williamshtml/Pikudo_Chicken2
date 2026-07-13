package com.pikudo.restaurant.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CatalogProductModifierGroupRequestDTO(
        @NotNull(message = "modifierGroupId es obligatorio")
        Long modifierGroupId,
        Boolean requiredOverride,
        @Min(value = 0, message = "minSelectionOverride no puede ser negativo")
        Integer minSelectionOverride,
        @Min(value = 0, message = "maxSelectionOverride no puede ser negativo")
        Integer maxSelectionOverride,
        Integer sortOrder
) {
}
