package com.pikudo.dto.catalog;

import com.pikudo.entity.AreaPreparacion;

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
