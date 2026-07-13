package com.pikudo.repository;

import com.pikudo.dto.reporte.ReporteDTO.FlujoHorarioDTO;
import com.pikudo.dto.reporte.ReporteDTO.ProductoMasVendidoDTO;
import com.pikudo.entity.EstadoPedido;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.orders.OrderOperationalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByMesaIdAndEstadoNot(Long mesaId, EstadoPedido estado);

    List<Pedido> findByMesaIdAndEstadoOperativoNotIn(Long mesaId, List<OrderOperationalStatus> statuses);

    boolean existsByTableSessionIdAndEstadoOperativoNotIn(Long tableSessionId, List<OrderOperationalStatus> statuses);

    @Modifying
    @Query("UPDATE Pedido p SET p.repartidor.id = :repartidorId, p.estado = 'ON_DELIVERY', p.estadoOperativo = 'ON_DELIVERY' " +
           "WHERE p.id = :pedidoId " +
           "AND p.estado = 'PENDING' " +
           "AND p.repartidor IS NULL")
    int asignarRepartidorAtomicamente(@Param("pedidoId") Long pedidoId, @Param("repartidorId") Long repartidorId);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estadoPago = 'PAID' AND p.fechaCreacion BETWEEN :desde AND :hasta")
    BigDecimal calcularTotalVentasPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT new com.pikudo.dto.reporte.ReporteDTO$ProductoMasVendidoDTO(" +
           "COALESCE(d.productoNombreSnapshot, d.producto.nombre), SUM(d.cantidad), SUM(d.lineTotal)) " +
           "FROM Pedido p JOIN p.detalles d " +
           "WHERE p.estadoPago = 'PAID' AND p.fechaCreacion BETWEEN :desde AND :hasta " +
           "GROUP BY COALESCE(d.productoNombreSnapshot, d.producto.nombre) " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<ProductoMasVendidoDTO> findProductosMasVendidos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT new com.pikudo.dto.reporte.ReporteDTO$FlujoHorarioDTO(HOUR(p.fechaCreacion), COUNT(p)) " +
           "FROM Pedido p " +
           "WHERE p.fechaCreacion BETWEEN :desde AND :hasta " +
           "GROUP BY HOUR(p.fechaCreacion) " +
           "ORDER BY HOUR(p.fechaCreacion) ASC")
    List<FlujoHorarioDTO> findFlujoHorarioPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
