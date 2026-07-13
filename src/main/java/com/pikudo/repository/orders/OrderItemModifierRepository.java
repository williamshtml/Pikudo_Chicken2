package com.pikudo.repository.orders;

import com.pikudo.entity.orders.OrderItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifier, Long> {

    List<OrderItemModifier> findByDetallePedidoIdOrderByIdAsc(Long detallePedidoId);
}
