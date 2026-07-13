package com.pikudo.dto.orders;

import com.pikudo.entity.orders.OrderPaymentStatusType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderPaymentResponseDTO {

    private Long id;
    private Long orderId;
    private Long cajaId;
    private String metodoPago;
    private String metodoPagoTipo;
    private BigDecimal monto;
    private OrderPaymentStatusType status;
    private String externalReference;
    private String notes;
    private String receivedByUsername;
    private String voidedByUsername;
    private String voidReason;
    private LocalDateTime fechaCreacion;
    private LocalDateTime voidedAt;
}
