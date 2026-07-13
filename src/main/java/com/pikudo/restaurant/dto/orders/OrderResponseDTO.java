package com.pikudo.restaurant.dto.orders;

import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderServiceType;
import com.pikudo.restaurant.entity.orders.OrderSource;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponseDTO {

    private Long id;
    private String orderCode;
    private String trackingCode;
    private Long mesaId;
    private Integer mesaNumero;
    private Long tableSessionId;
    private OrderOperationalStatus estadoOperativo;
    private OrderPaymentStatus estadoPago;
    private OrderServiceType serviceType;
    private OrderSource source;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private String direccion;
    private String telefonoCliente;
    private String observacionesPedido;
    private LocalDateTime fechaCreacion;
    private OrderPaymentSummaryDTO paymentSummary;
    private List<OrderDiscountResponseDTO> discounts;
    private List<OrderItemResponseDTO> items;
}
