package com.pikudo.service.orders;

import com.pikudo.dto.orders.OrderCreateRequestDTO;
import com.pikudo.dto.orders.OrderResponseDTO;
import com.pikudo.dto.orders.OrderStatusChangeRequestDTO;
import com.pikudo.dto.orders.OrderStatusHistoryResponseDTO;
import com.pikudo.entity.orders.OrderOperationalStatus;
import com.pikudo.entity.orders.OrderPaymentStatus;
import com.pikudo.entity.orders.OrderServiceType;
import com.pikudo.entity.orders.OrderSource;
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
