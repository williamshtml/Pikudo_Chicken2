package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.inventario.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByInsumoIdOrderByFechaCreacionDesc(Long insumoId);
}