package com.pikudo.repository;

import com.pikudo.entity.caja.Caja;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findByEstado(String estado);

    // NUEVO: lock pesimista para evitar doble apertura de caja en condición de carrera
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Caja c WHERE c.estado = 'ABIERTA'")
    Optional<Caja> findByEstadoConLock();
}