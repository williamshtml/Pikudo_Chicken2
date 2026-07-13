package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.AreaPreparacion;
import com.pikudo.restaurant.entity.Impresora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImpresoraRepository extends JpaRepository<Impresora, Long> {
    Optional<Impresora> findByAreaAndActivaTrue(AreaPreparacion area);
}