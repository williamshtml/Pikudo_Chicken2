package com.pikudo.repository;

import com.pikudo.entity.inventario.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    // Hereda los métodos básicos de persistencia (save, findAll, etc.)
}