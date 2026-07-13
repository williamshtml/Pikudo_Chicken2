package com.pikudo.restaurant.repository.catalog;

import com.pikudo.restaurant.entity.catalog.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    List<ProductoVariante> findByProductoIdOrderByOrdenAscIdAsc(Long productoId);
}
