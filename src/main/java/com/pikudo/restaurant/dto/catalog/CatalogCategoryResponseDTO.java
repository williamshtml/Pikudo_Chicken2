package com.pikudo.restaurant.dto.catalog;

import com.pikudo.restaurant.entity.AreaPreparacion;

public record CatalogCategoryResponseDTO(
        Long id,
        String name,
        String slug,
        String description,
        Integer sortOrder,
        Boolean publicVisible,
        Boolean available,
        Boolean active,
        AreaPreparacion preparationArea
) {
}
