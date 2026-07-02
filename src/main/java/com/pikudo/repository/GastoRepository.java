package com.pikudo.repository;

import com.pikudo.entity.caja.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByCajaId(Long cajaId);
    
    @Query("SELECT SUM(g.monto) FROM Gasto g WHERE g.fechaCreacion BETWEEN :desde AND :hasta")
    BigDecimal calcularTotalGastosPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}