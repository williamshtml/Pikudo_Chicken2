package com.pikudo.repository;

import com.pikudo.entity.caja.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {
    Optional<Caja> findByEstado(String estado);
}