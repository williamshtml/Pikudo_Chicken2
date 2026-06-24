package com.pikudo.repository;
import com.pikudo.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Para buscar el rol rápidamente al asignar permisos (ej. "ADMINISTRADOR")
    Optional<Rol> findByNombre(String nombre);
}
