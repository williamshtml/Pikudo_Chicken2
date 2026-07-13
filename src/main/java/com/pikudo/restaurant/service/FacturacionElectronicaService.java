package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.sunat.ResultadoEnvioSunatDTO;
import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.NotaCredito;

/**
 * Punto único de integración con SUNAT/OSE/proveedor externo de facturación
 * electrónica. Mientras no se conecte un proveedor real, la implementación
 * activa es FacturacionElectronicaServiceImpl (stub que marca todo como
 * NO_ENVIADO). Cuando se elija el proveedor, SOLO se reemplaza la lógica
 * interna de esa clase — ningún otro archivo del sistema necesita tocarse.
 */
public interface FacturacionElectronicaService {
    ResultadoEnvioSunatDTO enviarComprobante(Comprobante comprobante);
    ResultadoEnvioSunatDTO enviarNotaCredito(NotaCredito notaCredito);
}