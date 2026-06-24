package com.pikudo.repository;

import com.pikudo.entity.Pedido;
import com.pikudo.entity.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Para la pantalla de cocina: busca los pedidos en estado 'IN_KITCHEN'
    List<Pedido> findByEstado(EstadoPedido estado);
    
    // Para saber qué órdenes tiene abiertas una mesa específica
    List<Pedido> findByMesaIdAndEstadoNot(Long mesaId, EstadoPedido estado);
}