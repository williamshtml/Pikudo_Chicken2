package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.TipoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    // Búsqueda para evitar duplicados en la facturación electrónica
    Optional<Comprobante> findBySerieAndCorrelativo(String serie, String correlativo);

    // Método necesario para obtener el siguiente correlativo según el tipo (Factura o Boleta)
    long countByTipoComprobante(TipoComprobante tipoComprobante);

    boolean existsByPedidoId(Long pedidoId);

    // NUEVO: historial de ventas filtrado por rango de fechas
    @Query("SELECT c FROM Comprobante c WHERE c.fechaEmision BETWEEN :inicio AND :fin ORDER BY c.fechaEmision DESC")
    List<Comprobante> findByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);
}
