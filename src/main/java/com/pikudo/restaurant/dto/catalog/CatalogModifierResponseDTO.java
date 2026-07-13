package com.pikudo.restaurant.dto.catalog;

import java.math.BigDecimal;

public record CatalogModifierResponseDTO(
        Long id,
        String name,
        String slug,
        BigDecimal extraPrice,
        Boolean active,
        Boolean publicVisible,
        Integer sortOrder
) {
}
