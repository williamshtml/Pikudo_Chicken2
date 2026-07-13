package com.pikudo.restaurant.dto.orders;

import com.pikudo.restaurant.entity.orders.OrderDiscountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDiscountRequestDTO {

    private Long detailId;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private OrderDiscountType discountType;

    @NotNull(message = "El valor del descuento es obligatorio")
    @Positive(message = "El descuento debe ser mayor a cero")
    private BigDecimal value;

    @NotNull(message = "El motivo es obligatorio")
    @Size(max = 300, message = "El motivo no puede superar los 300 caracteres")
    private String reason;
}
