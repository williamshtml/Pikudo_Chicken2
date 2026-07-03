package com.pikudo.service;

import com.pikudo.dto.inventario.InsumoDTO;
import com.pikudo.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.dto.inventario.RecetaDTO;

import java.math.BigDecimal;
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
}