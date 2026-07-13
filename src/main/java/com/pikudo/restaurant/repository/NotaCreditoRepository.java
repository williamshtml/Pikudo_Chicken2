package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.NotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {
    long count();
}