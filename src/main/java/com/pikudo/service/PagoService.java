package com.pikudo.service;

import com.pikudo.entity.Pedido;
import com.pikudo.entity.caja.MetodoPago;

/**
 * Encapsula la logica de negocio alrededor del metodo de pago de un pedido:
 * validar que el metodo exista y este activo, y asignarlo correctamente.
 * Es la pieza que faltaba para que las queries de cuadre de caja
 * (PedidoRepository.calcularTotalVentasPorMetodoTipo) tengan datos reales.
 */
public interface PagoService {

    /**
     * Busca un metodo de pago por nombre (ej. "EFECTIVO", "YAPE") y valida que este activo.
     * Lanza excepcion si no existe o esta desactivado.
     */
    MetodoPago resolverMetodoPago(String nombre);

    /**
     * Resuelve el metodo de pago y lo asigna al pedido. No guarda el pedido
     * (eso lo hace quien orquesta, ej. ComprobanteServiceImpl), solo deja
     * la entidad lista para persistir.
     */
    void aplicarMetodoPago(Pedido pedido, String nombreMetodoPago);
}