package com.pikudo.entity;

public enum EstadoPedido {
    PENDING,      // Mozo tomó el pedido, se imprime ticket a cocina/bar/hornos
    PAID,         // Cliente pagó en caja
    ON_DELIVERY,  // Salió con el repartidor
    DELIVERED,    // Repartidor confirmó entrega
    CANCELLED     // Pedido cancelado
}
