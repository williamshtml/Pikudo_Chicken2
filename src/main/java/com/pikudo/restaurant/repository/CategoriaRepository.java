package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long>, JpaSpecificationExecutor<Categoria> {

    boolean existsBySlug(String slug);

    Optional<Categoria> findBySlug(String slug);
}
