package com.pikudo.repository;

import com.pikudo.entity.inventario.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    @Query("SELECT i FROM Insumo i WHERE i.stockActual <= i.stockMinimo AND i.estado = true")
    List<Insumo> findInsumosAlertaStock();

    Optional<Insumo> findByNombreIgnoreCase(String nombre);

    // NUEVO: descuento atómico en BD, evita "lost update" en concurrencia
    @Modifying
    @Query("UPDATE Insumo i SET i.stockActual = i.stockActual - :cantidad WHERE i.id = :insumoId")
    int descontarStockAtomico(Long insumoId, BigDecimal cantidad);

    // NUEVO: incremento atómico (para ingresos y reversiones por cancelación)
    @Modifying
    @Query("UPDATE Insumo i SET i.stockActual = i.stockActual + :cantidad WHERE i.id = :insumoId")
    int incrementarStockAtomico(Long insumoId, BigDecimal cantidad);
}