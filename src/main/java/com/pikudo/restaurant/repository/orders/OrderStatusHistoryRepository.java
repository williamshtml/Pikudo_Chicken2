package com.pikudo.restaurant.repository.orders;

import com.pikudo.restaurant.entity.orders.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByPedidoIdOrderByFechaCreacionAscIdAsc(Long pedidoId);
}
