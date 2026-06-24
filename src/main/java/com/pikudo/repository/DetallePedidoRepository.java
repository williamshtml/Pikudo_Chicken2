package com.pikudo.repository;

import com.pikudo.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    // Para consultar las filas pertenecientes a un pedido específico si es necesario
    List<DetallePedido> findByPedidoId(Long pedidoId);
}