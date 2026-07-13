package com.pikudo.restaurant.dto.catalog;

import java.math.BigDecimal;

public record CatalogProductVariantResponseDTO(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        Integer stock,
        Boolean publicVisible,
        Boolean available,
        Integer sortOrder
) {
}
