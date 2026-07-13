package com.pikudo.repository.orders;

import com.pikudo.entity.orders.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByPedidoIdOrderByFechaCreacionAscIdAsc(Long pedidoId);
}
