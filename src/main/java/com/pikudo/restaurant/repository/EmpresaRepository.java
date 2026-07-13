package com.pikudo.restaurant.repository;

import com.pikudo.restaurant.entity.ConfiguracionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<ConfiguracionEmpresa, Long> {
}