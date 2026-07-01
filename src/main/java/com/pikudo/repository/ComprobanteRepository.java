package com.pikudo.repository;

import com.pikudo.entity.Comprobante;
import com.pikudo.entity.TipoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    // Búsqueda para evitar duplicados en la facturación electrónica
    Optional<Comprobante> findBySerieAndCorrelativo(String serie, String correlativo);

    // Método necesario para obtener el siguiente correlativo según el tipo (Factura o Boleta)
    long countByTipoComprobante(TipoComprobante tipoComprobante);
}