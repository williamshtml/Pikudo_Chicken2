package com.pikudo.restaurant.dto.catalog;

import java.math.BigDecimal;

public record CatalogComboComponentResponseDTO(
        Long id,
        Long comboProductId,
        Long componentVariantId,
        Long componentProductId,
        String componentProductName,
        String componentVariantName,
        BigDecimal quantity,
        Boolean required,
        Boolean replaceable,
        Integer sortOrder
) {
}
