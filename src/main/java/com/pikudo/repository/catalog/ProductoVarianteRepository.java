package com.pikudo.repository.catalog;

import com.pikudo.entity.catalog.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    List<ProductoVariante> findByProductoIdOrderByOrdenAscIdAsc(Long productoId);
}
