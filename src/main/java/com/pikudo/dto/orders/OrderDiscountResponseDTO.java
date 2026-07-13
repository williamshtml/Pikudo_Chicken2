package com.pikudo.dto.orders;

import com.pikudo.entity.orders.OrderDiscountStatus;
import com.pikudo.entity.orders.OrderDiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderDiscountResponseDTO {

    private Long id;
    private Long orderId;
    private Long detailId;
    private OrderDiscountType discountType;
    private BigDecimal requestedValue;
    private BigDecimal calculatedAmount;
    private String reason;
    private String authorizedByUsername;
    private OrderDiscountStatus status;
    private LocalDateTime fechaCreacion;
}
