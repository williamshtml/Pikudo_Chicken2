package com.pikudo.restaurant.dto.catalog;

public record CatalogProductModifierGroupResponseDTO(
        Long id,
        Long productId,
        CatalogModifierGroupResponseDTO group,
        Boolean required,
        Integer minSelection,
        Integer maxSelection,
        Integer sortOrder
) {
}
