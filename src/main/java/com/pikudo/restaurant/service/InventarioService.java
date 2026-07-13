package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.inventario.InsumoDTO;
import com.pikudo.restaurant.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.restaurant.dto.inventario.RecetaDTO;

import java.util.List;

public interface InventarioService {

    // Gestión de Insumos
    InsumoDTO crearInsumo(InsumoDTO dto);
    List<InsumoDTO> listarInsumosBajoStockMinimo();

    // Gestión de Recetas
    RecetaDTO registrarInsumoEnReceta(RecetaDTO dto);
    List<RecetaDTO> obtenerRecetaPorProducto(Long productoId);

    // Movimientos y Descuentos Críticos
    MovimientoInventarioDTO registrarMovimientoManual(MovimientoInventarioDTO dto);
    void descontarStockPorVenta(Long productoId, Integer cantidadVendida);

    // NUEVO: revierte el descuento cuando un pedido se cancela
    void revertirStockPorCancelacion(Long productoId, Integer cantidadCancelada);
}