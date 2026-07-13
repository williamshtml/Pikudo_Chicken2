package com.pikudo.repository;

import com.pikudo.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    List<Producto> findByEstadoTrue();

    List<Producto> findByCategoriaIdAndEstadoTrue(Long categoriaId);

    boolean existsBySlug(String slug);

    Optional<Producto> findBySlug(String slug);
}
