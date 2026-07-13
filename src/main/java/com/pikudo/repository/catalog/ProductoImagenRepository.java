package com.pikudo.repository.catalog;

import com.pikudo.entity.catalog.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    List<ProductoImagen> findByProductoIdOrderByPrincipalDescOrdenAscIdAsc(Long productoId);
}
