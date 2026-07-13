package com.pikudo.service.orders;

import com.pikudo.entity.Pedido;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.orders.OrderOperationalStatus;

public interface OrderTransitionService {

    Pedido transition(Pedido pedido, OrderOperationalStatus targetStatus, Usuario changedBy, String reason);

    void recordInitialStatus(Pedido pedido, Usuario changedBy, String reason);
}
