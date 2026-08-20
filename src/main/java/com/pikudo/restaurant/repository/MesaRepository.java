package com.pikudo.restaurant.repository;
import com.pikudo.restaurant.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    // Para que el mozo vea qué mesas están libres para sentar clientes nuevos
    List<Mesa> findByEstado(Boolean estado);
    // Necesario para validar numeros duplicados con un mensaje claro,
    // en vez de dejar que explote la restriccion unique de la base de datos
    Optional<Mesa> findByNumero(Integer numero);
}