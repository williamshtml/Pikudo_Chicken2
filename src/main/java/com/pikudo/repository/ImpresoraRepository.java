package com.pikudo.repository;

import com.pikudo.entity.AreaPreparacion;
import com.pikudo.entity.Impresora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImpresoraRepository extends JpaRepository<Impresora, Long> {
    Optional<Impresora> findByAreaAndActivaTrue(AreaPreparacion area);
}