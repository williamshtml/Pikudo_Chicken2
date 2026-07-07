package com.pikudo.repository;

import com.pikudo.entity.caja.TransaccionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {

    List<TransaccionPago> findByComprobanteId(Long comprobanteId);

    // Reemplaza a PedidoRepository.calcularTotalVentasPorMetodoTipo, que ya no es
    // confiable ahora que un pedido puede tener varios metodos de pago a la vez.
    @Query("SELECT SUM(t.monto) FROM TransaccionPago t " +
           "WHERE t.metodoPago.tipo = :tipoMetodo " +
           "AND t.comprobante.pedido.estado = 'PAID' " +
           "AND t.fechaCreacion BETWEEN :desde AND :hasta")
    BigDecimal calcularTotalPorTipoMetodo(
            @Param("tipoMetodo") String tipoMetodo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}