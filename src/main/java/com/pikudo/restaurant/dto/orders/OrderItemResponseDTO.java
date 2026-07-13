package com.pikudo.restaurant.dto.orders;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private String notes;
    private List<OrderItemModifierResponseDTO> modifiers;
}
