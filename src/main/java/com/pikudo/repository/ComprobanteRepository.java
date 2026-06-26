package com.pikudo.repository;

import com.pikudo.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    // Para evitar duplicación de documentos en la facturación financiera
    Optional<Comprobante> findBySerieAndCorrelativo(String serie, String correlativo);
}