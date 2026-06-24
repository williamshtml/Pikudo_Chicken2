package com.pikudo.repository;

import com.pikudo.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Para mostrar en la tablet/celular del mozo solo lo que está activo para vender
    List<Producto> findByEstadoTrue();
    
    // Para filtrar la carta por categoría (ej. mostrar solo "Bebidas" o "Pollos")
    List<Producto> findByCategoriaIdAndEstadoTrue(Long categoriaId);
}