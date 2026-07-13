package com.pikudo.dto.orders;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemModifierResponseDTO {

    private Long id;
    private Long modifierId;
    private Long modifierGroupId;
    private String modifierGroupName;
    private String modifierName;
    private Integer quantity;
    private BigDecimal extraPrice;
    private BigDecimal totalExtra;
}
