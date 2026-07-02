package com.pikudo.repository;

import com.pikudo.entity.inventario.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    @Query("SELECT i FROM Insumo i WHERE i.stockActual <= i.stockMinimo AND i.estado = true")
    List<Insumo> findInsumosAlertaStock();
}