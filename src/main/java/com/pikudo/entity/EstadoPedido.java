package com.pikudo.entity;

public enum EstadoPedido {
    PENDING,     // El mozo tomó el pedido pero aún no se envía a cocina
    IN_KITCHEN,  // El pedido ya está siendo preparado por los cocineros
    PAID,        // El cliente ya pagó la cuenta en caja
    CANCELLED    // El pedido fue cancelado (por error de digitación, falta de stock, etc.)
}
