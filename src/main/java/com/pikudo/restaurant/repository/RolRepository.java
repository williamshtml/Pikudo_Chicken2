package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.Rol;
import com.pikudo.restaurant.entity.Rol.TipoRol; // <-- Importas tu Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Cambiamos String por TipoRol
    Optional<Rol> findByNombre(TipoRol nombre); 
}