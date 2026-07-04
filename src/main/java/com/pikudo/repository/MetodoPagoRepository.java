package com.pikudo.repository;

import com.pikudo.entity.caja.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    List<MetodoPago> findByActivoTrue();

    // Ignora mayusculas/minusculas para que "efectivo" y "EFECTIVO" resuelvan igual
    Optional<MetodoPago> findByNombreIgnoreCase(String nombre);
}