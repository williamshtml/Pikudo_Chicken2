package com.pikudo.service.impl;

import com.pikudo.dto.sunat.ResultadoEnvioSunatDTO;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.EstadoSunat;
import com.pikudo.entity.NotaCredito;
import com.pikudo.service.FacturacionElectronicaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * IMPLEMENTACIÓN TEMPORAL (stub). No envía nada a SUNAT todavía.
 *
 * CUANDO SE ELIJA EL PROVEEDOR, REEMPLAZAR AQUÍ:
 *
 * Opción A - Proveedor externo (Facturalaya, PANCA, etc.):
 *   1. Armar el payload con los datos del Comprobante
 *   2. Llamar su endpoint REST (RestTemplateConfig.java ya existe en com.pikudo.config)
 *   3. Mapear su respuesta al ResultadoEnvioSunatDTO
 *
 * Opción B - SEE del Contribuyente (implementación propia):
 *   1. Generar XML UBL 2.1, firmar digitalmente, enviar a SUNAT, procesar CDR
 *
 * El resto del sistema ya está preparado para recibir el resultado y
 * guardarlo — no hay que tocar nada más fuera de esta clase.
 */
@Service
@Slf4j
public class FacturacionElectronicaServiceImpl implements FacturacionElectronicaService {

    @Override
    public ResultadoEnvioSunatDTO enviarComprobante(Comprobante comprobante) {
        log.info("SUNAT aún no está conectado. Comprobante {}-{} queda como NO_ENVIADO.",
                comprobante.getSerie(), comprobante.getCorrelativo());
        return ResultadoEnvioSunatDTO.builder()
                .estado(EstadoSunat.NO_ENVIADO)
                .mensaje("Integración con SUNAT pendiente de configurar")
                .build();
    }

    @Override
    public ResultadoEnvioSunatDTO enviarNotaCredito(NotaCredito notaCredito) {
        log.info("SUNAT aún no está conectado. Nota de crédito {}-{} queda como NO_ENVIADO.",
                notaCredito.getSerie(), notaCredito.getCorrelativo());
        return ResultadoEnvioSunatDTO.builder()
                .estado(EstadoSunat.NO_ENVIADO)
                .mensaje("Integración con SUNAT pendiente de configurar")
                .build();
    }
}