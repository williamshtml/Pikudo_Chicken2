package com.pikudo.dto.catalog;

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
