package com.pikudo.restaurant.repository.catalog;

import com.pikudo.restaurant.entity.catalog.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    List<ProductoImagen> findByProductoIdOrderByPrincipalDescOrdenAscIdAsc(Long productoId);
}
