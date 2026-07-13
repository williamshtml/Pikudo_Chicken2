package com.pikudo.restaurant.service.impresion;

import com.pikudo.restaurant.entity.Impresora;

/**
 * Sabe unicamente "como hablarle" a una impresora fisica (protocolo de transporte).
 * No sabe nada de pedidos, boletas ni formato de contenido - eso lo resuelven
 * las clases de com.pikudo.restaurant.service.impresion.formatos.
 */
public interface MotorImpresion {
    void enviarComandos(Impresora impresora, byte[] comandos);
}