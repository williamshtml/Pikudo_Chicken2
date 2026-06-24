
package com.pikudo.repository;

import com.pikudo.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Suficiente con los métodos heredados para el mantenimiento de familias
}