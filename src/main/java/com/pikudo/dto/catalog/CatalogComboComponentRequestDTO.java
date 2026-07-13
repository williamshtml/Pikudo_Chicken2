package com.pikudo.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CatalogComboComponentRequestDTO(
        @NotNull(message = "componentVariantId es obligatorio")
        Long componentVariantId,
        @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
        BigDecimal quantity,
        Boolean required,
        Boolean replaceable,
        Integer sortOrder
) {
}
