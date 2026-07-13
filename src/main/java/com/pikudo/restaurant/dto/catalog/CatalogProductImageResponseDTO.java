package com.pikudo.restaurant.dto.catalog;

import java.util.UUID;

public record CatalogProductImageResponseDTO(
        Long id,
        UUID storageFileId,
        Long variantId,
        String url,
        Boolean primary,
        Integer sortOrder,
        String altText,
        Boolean publicVisible
) {
}
