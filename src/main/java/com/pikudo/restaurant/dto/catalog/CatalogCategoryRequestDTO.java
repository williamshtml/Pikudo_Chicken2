package com.pikudo.restaurant.dto.catalog;

import com.pikudo.restaurant.entity.AreaPreparacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogCategoryRequestDTO(
        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        String name,
        @Size(max = 90, message = "El slug no puede superar los 90 caracteres")
        String slug,
        @Size(max = 250, message = "La descripcion no puede superar los 250 caracteres")
        String description,
        Integer sortOrder,
        Boolean publicVisible,
        Boolean available,
        Boolean active,
        AreaPreparacion preparationArea
) {
}
