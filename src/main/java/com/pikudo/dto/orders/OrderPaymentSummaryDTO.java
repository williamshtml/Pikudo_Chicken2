package com.pikudo.dto.orders;

import com.pikudo.entity.orders.OrderPaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderPaymentSummaryDTO {

    private Long orderId;
    private BigDecimal total;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private OrderPaymentStatus paymentStatus;
}
