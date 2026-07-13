package com.pikudo.restaurant.repository.orders;

import com.pikudo.restaurant.entity.orders.OrderItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifier, Long> {

    List<OrderItemModifier> findByDetallePedidoIdOrderByIdAsc(Long detallePedidoId);
}
