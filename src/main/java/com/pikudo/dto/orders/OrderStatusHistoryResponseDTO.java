package com.pikudo.dto.orders;

import com.pikudo.entity.orders.OrderOperationalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderStatusHistoryResponseDTO {

    private Long id;
    private OrderOperationalStatus fromStatus;
    private OrderOperationalStatus toStatus;
    private String changedByUsername;
    private String reason;
    private LocalDateTime fechaCreacion;
}
