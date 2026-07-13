package com.pikudo.dto.catalog;

import java.util.List;

public record CatalogModifierGroupResponseDTO(
        Long id,
        String name,
        String slug,
        String description,
        Integer minSelection,
        Integer maxSelection,
        Boolean required,
        Boolean active,
        Boolean publicVisible,
        Integer sortOrder,
        List<CatalogModifierResponseDTO> modifiers
) {
}
