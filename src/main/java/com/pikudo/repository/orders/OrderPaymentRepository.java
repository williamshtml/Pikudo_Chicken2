package com.pikudo.repository.orders;

import com.pikudo.entity.orders.OrderPayment;
import com.pikudo.entity.orders.OrderPaymentStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    List<OrderPayment> findByPedidoIdOrderByFechaCreacionAscIdAsc(Long pedidoId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM OrderPayment p WHERE p.pedido.id = :pedidoId AND p.status = :status")
    BigDecimal sumByPedidoIdAndStatus(@Param("pedidoId") Long pedidoId, @Param("status") OrderPaymentStatusType status);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM OrderPayment p " +
           "WHERE p.caja.id = :cajaId AND p.status = com.pikudo.entity.orders.OrderPaymentStatusType.CONFIRMED")
    BigDecimal sumConfirmedByCajaId(@Param("cajaId") Long cajaId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM OrderPayment p " +
           "WHERE p.caja.id = :cajaId AND p.metodoPago.tipo = :tipoMetodo " +
           "AND p.status = com.pikudo.entity.orders.OrderPaymentStatusType.CONFIRMED")
    BigDecimal sumConfirmedByCajaIdAndTipoMetodo(@Param("cajaId") Long cajaId, @Param("tipoMetodo") String tipoMetodo);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM OrderPayment p " +
           "WHERE p.metodoPago.tipo = :tipoMetodo " +
           "AND p.status = com.pikudo.entity.orders.OrderPaymentStatusType.CONFIRMED " +
           "AND p.fechaCreacion BETWEEN :desde AND :hasta")
    BigDecimal sumConfirmedByTipoMetodoAndFecha(@Param("tipoMetodo") String tipoMetodo,
                                                 @Param("desde") LocalDateTime desde,
                                                 @Param("hasta") LocalDateTime hasta);

    boolean existsByCajaIdAndPedidoEstadoPago(Long cajaId, com.pikudo.entity.orders.OrderPaymentStatus estadoPago);
}
