package com.pikudo.repository;

import com.pikudo.entity.Pedido;
import com.pikudo.entity.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.pikudo.dto.reporte.ReporteDTO.ProductoMasVendidoDTO;
import com.pikudo.dto.reporte.ReporteDTO.FlujoHorarioDTO;
import org.springframework.data.jpa.repository.Modifying; // <-- Agregado
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // Para la pantalla de cocina: busca los pedidos en estado 'IN_KITCHEN'
    List<Pedido> findByEstado(EstadoPedido estado);
    
    // Para saber qué órdenes tiene abiertas una mesa específica
    List<Pedido> findByMesaIdAndEstadoNot(Long mesaId, EstadoPedido estado);
    
    // QUERY ATÓMICA ANTI-CARRERAS PARA MOTORIZADOS
    @Modifying
    @Query("UPDATE Pedido p SET p.repartidor.id = :repartidorId, p.estado = 'ON_DELIVERY' " +
           "WHERE p.id = :pedidoId " +
           "AND p.estado = 'PENDING' " +
           "AND p.repartidor IS NULL")
    int asignarRepartidorAtomicamente(@Param("pedidoId") Long pedidoId, @Param("repartidorId") Long repartidorId);
    
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado = 'PAID' AND p.fechaCreacion BETWEEN :desde AND :hasta")
    BigDecimal calcularTotalVentasPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT new com.pikudo.dto.reporte.ReporteDTO$ProductoMasVendidoDTO(d.producto.nombre, SUM(d.cantidad), SUM(d.subtotal)) " +
           "FROM Pedido p JOIN p.detalles d " +
           "WHERE p.estado = 'PAID' AND p.fechaCreacion BETWEEN :desde AND :hasta " +
           "GROUP BY d.producto.nombre " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<ProductoMasVendidoDTO> findProductosMasVendidos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT new com.pikudo.dto.reporte.ReporteDTO$FlujoHorarioDTO(HOUR(p.fechaCreacion), COUNT(p)) " +
           "FROM Pedido p " +
           "WHERE p.fechaCreacion BETWEEN :desde AND :hasta " +
           "GROUP BY HOUR(p.fechaCreacion) " +
           "ORDER BY HOUR(p.fechaCreacion) ASC")
    List<FlujoHorarioDTO> findFlujoHorarioPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}