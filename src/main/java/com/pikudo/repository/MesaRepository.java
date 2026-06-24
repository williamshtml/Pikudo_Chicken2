package com.pikudo.repository;

import com.pikudo.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    // Para que el mozo vea qué mesas están libres para sentar clientes nuevos
    List<Mesa> findByEstado(String estado);
}