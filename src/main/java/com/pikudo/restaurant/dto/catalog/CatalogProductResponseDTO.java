package com.pikudo.restaurant.dto.catalog;

import com.pikudo.restaurant.entity.ProductoTipo;

import java.math.BigDecimal;
import java.util.List;

public record CatalogProductResponseDTO(
        Long id,
        String name,
        String slug,
        String description,
        ProductoTipo type,
        BigDecimal currentPrice,
        Integer stock,
        Boolean publicVisible,
        Boolean available,
        Boolean active,
        Integer sortOrder,
        CatalogCategoryResponseDTO category,
        List<CatalogProductVariantResponseDTO> variants,
        CatalogProductImageResponseDTO mainImage,
        List<CatalogProductModifierGroupResponseDTO> modifierGroups,
        List<CatalogComboComponentResponseDTO> comboComponents
) {
}
