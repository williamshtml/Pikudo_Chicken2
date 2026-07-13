package com.pikudo.restaurant.repository.orders;

import com.pikudo.restaurant.entity.orders.OrderDiscount;
import com.pikudo.restaurant.entity.orders.OrderDiscountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDiscountRepository extends JpaRepository<OrderDiscount, Long> {

    List<OrderDiscount> findByPedidoIdAndStatusOrderByFechaCreacionAscIdAsc(Long pedidoId, OrderDiscountStatus status);

    List<OrderDiscount> findByPedidoIdOrderByFechaCreacionAscIdAsc(Long pedidoId);
}
