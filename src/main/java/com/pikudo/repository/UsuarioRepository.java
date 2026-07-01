package com.pikudo.repository;
import com.pikudo.entity.Rol.TipoRol;
import com.pikudo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Crítico para el Login: busca el usuario por su cuenta de acceso
    Optional<Usuario> findByUsername(String username);
    
    // Para listar el personal que está laborando actualmente
    List<Usuario> findByEstadoTrue();

    // Para el panel de presencia: trae a todos los usuarios con rol MOTORIZADO,
    // sin importar cuantos haya ni si rotan a diario
    List<Usuario> findByRol_Nombre(TipoRol rol);
}