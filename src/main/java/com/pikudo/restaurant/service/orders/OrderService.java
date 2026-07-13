package com.pikudo.restaurant.service.orders;

import com.pikudo.restaurant.dto.orders.OrderCreateRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderStatusChangeRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderStatusHistoryResponseDTO;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderServiceType;
import com.pikudo.restaurant.entity.orders.OrderSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    Page<OrderResponseDTO> list(
            OrderOperationalStatus operationalStatus,
            OrderPaymentStatus paymentStatus,
            Long mesaId,
            OrderServiceType serviceType,
            OrderSource source,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    OrderResponseDTO create(OrderCreateRequestDTO request);

    OrderResponseDTO get(Long id);

    OrderResponseDTO transition(Long id, OrderStatusChangeRequestDTO request);

    List<OrderStatusHistoryResponseDTO> history(Long id);
}
