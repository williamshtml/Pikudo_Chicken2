package com.pikudo.restaurant.service.orders;

import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;

public interface OrderTransitionService {

    Pedido transition(Pedido pedido, OrderOperationalStatus targetStatus, Usuario changedBy, String reason);

    void recordInitialStatus(Pedido pedido, Usuario changedBy, String reason);
}
