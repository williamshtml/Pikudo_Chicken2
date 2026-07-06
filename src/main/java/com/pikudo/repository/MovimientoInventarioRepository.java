package com.pikudo.repository;

import com.pikudo.entity.inventario.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByInsumoIdOrderByFechaCreacionDesc(Long insumoId);
}