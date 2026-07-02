package com.pikudo.repository;

import com.pikudo.entity.inventario.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByProductoId(Long productoId);
}