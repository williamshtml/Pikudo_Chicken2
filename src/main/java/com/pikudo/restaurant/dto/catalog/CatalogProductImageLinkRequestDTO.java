package com.pikudo.restaurant.dto.catalog;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CatalogProductImageLinkRequestDTO(
        @NotNull(message = "storageFileId es obligatorio")
        UUID storageFileId,
        Long variantId,
        Boolean primary,
        Integer sortOrder,
        @Size(max = 160, message = "El texto alternativo no puede superar los 160 caracteres")
        String altText,
        Boolean publicVisible
) {
}
