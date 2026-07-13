package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.comprobante.PagoDetalleDTO;
import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.caja.MetodoPago;
import com.pikudo.restaurant.entity.caja.TransaccionPago;

import java.math.BigDecimal;
import java.util.List;

/**
 * Encapsula la logica de negocio de los pagos: validar metodos, y construir
 * las transacciones de pago de un comprobante, soportando pagos divididos
 * (ej: mitad efectivo, mitad Yape) siempre que la suma coincida con el total.
 */
public interface PagoService {

    /**
     * Busca un metodo de pago por nombre (ej. "EFECTIVO", "YAPE") y valida que este activo.
     * Lanza excepcion si no existe o esta desactivado.
     */
    MetodoPago resolverMetodoPago(String nombre);

    /**
     * Valida la lista de pagos (cada metodo existe/activo, montos positivos, y la suma
     * coincide exactamente con el total del comprobante) y construye las entidades
     * TransaccionPago listas para asociar al comprobante. No las guarda directamente;
     * eso lo hace el orquestador (ComprobanteServiceImpl) al persistir el comprobante
     * con cascade.
     */
    List<TransaccionPago> procesarPagos(Comprobante comprobante, List<PagoDetalleDTO> pagos, BigDecimal montoEsperado);
}