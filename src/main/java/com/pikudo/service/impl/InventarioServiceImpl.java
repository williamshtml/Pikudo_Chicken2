package com.pikudo.service.impl;

import com.pikudo.dto.inventario.InsumoDTO;
import com.pikudo.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.dto.inventario.RecetaDTO;
import com.pikudo.entity.Producto;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.inventario.Insumo;
import com.pikudo.entity.inventario.MovimientoInventario;
import com.pikudo.entity.inventario.Receta;
import com.pikudo.entity.inventario.TipoMovimiento;
import com.pikudo.exception.BusinessException;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.mapper.InventarioMapper;
import com.pikudo.repository.InsumoRepository;
import com.pikudo.repository.MovimientoInventarioRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.RecetaRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InsumoRepository insumoRepository;
    private final RecetaRepository recetaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioMapper inventarioMapper;

    // ---- Insumos ----

    @Override
    public InsumoDTO crearInsumo(InsumoDTO dto) {
        insumoRepository.findByNombreIgnoreCase(dto.getNombre()).ifPresent(i -> {
            throw new BusinessException("Ya existe un insumo con el nombre: " + dto.getNombre());
        });
        Insumo insumo = inventarioMapper.toEntity(dto);
        return inventarioMapper.toDTO(insumoRepository.save(insumo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsumoDTO> listarInsumosBajoStockMinimo() {
        return insumoRepository.findInsumosAlertaStock().stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ---- Recetas ----

    @Override
    public RecetaDTO registrarInsumoEnReceta(RecetaDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + dto.getProductoId()));
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado: " + dto.getInsumoId()));

        Receta receta = inventarioMapper.toEntity(dto, producto, insumo);
        return inventarioMapper.toDTO(recetaRepository.save(receta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecetaDTO> obtenerRecetaPorProducto(Long productoId) {
        return recetaRepository.findByProductoId(productoId).stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ---- Movimientos ----

    @Override
    public MovimientoInventarioDTO registrarMovimientoManual(MovimientoInventarioDTO dto) {
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado: " + dto.getInsumoId()));

        Usuario usuario = usuarioActual();

        TipoMovimiento tipo = TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase());
        aplicarMovimientoAlStock(insumo, dto.getCantidad(), tipo);

        MovimientoInventario movimiento = inventarioMapper.toEntity(dto, insumo, usuario);
        insumoRepository.save(insumo);
        return inventarioMapper.toDTO(movimientoRepository.save(movimiento));
    }

    @Override
    public void descontarStockPorVenta(Long productoId, Integer cantidadVendida) {
        List<Receta> receta = recetaRepository.findByProductoId(productoId);

        if (receta.isEmpty()) {
            // No todos los productos tienen receta configurada (ej. bebidas envasadas
            // que no llevan preparacion); no es un error, simplemente no hay nada que descontar
            return;
        }

        Usuario usuario = usuarioActualOSistema();

        for (Receta r : receta) {
            Insumo insumo = r.getInsumo();
            BigDecimal cantidadADescontar = r.getCantidad().multiply(BigDecimal.valueOf(cantidadVendida));

            aplicarMovimientoAlStock(insumo, cantidadADescontar, TipoMovimiento.EGRESO);
            insumoRepository.save(insumo);

            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .insumo(insumo)
                    .cantidad(cantidadADescontar)
                    .tipoMovimiento(TipoMovimiento.EGRESO)
                    .motivo("Venta automática - Producto #" + productoId)
                    .usuario(usuario)
                    .build();
            movimientoRepository.save(movimiento);
        }
    }

    // ---- Helpers privados ----

    private void aplicarMovimientoAlStock(Insumo insumo, BigDecimal cantidad, TipoMovimiento tipo) {
        if (tipo == TipoMovimiento.INGRESO) {
            insumo.setStockActual(insumo.getStockActual().add(cantidad));
        } else {
            BigDecimal nuevoStock = insumo.getStockActual().subtract(cantidad);
            if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                // Advertencia, no bloqueo: se permite que el stock quede en negativo
                // para reflejar el faltante real, sin frenar la venta. Cuando el
                // inventario este mas maduro y confiable, esto se puede endurecer
                // a un BusinessException que bloquee la operacion.
                log.warn("Stock negativo para '{}': quedará en {} {} (se intentó descontar {})",
                        insumo.getNombre(), nuevoStock, insumo.getUnidadMedida(), cantidad);
            }
            insumo.setStockActual(nuevoStock);
        }
    }

    private Usuario usuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesión no encontrado"));
    }

    private Usuario usuarioActualOSistema() {
        // El descuento por venta puede dispararse en un contexto sin usuario autenticado
        // explicito (ej. un job interno); si no hay sesion, se registra sin usuario.
        try {
            return usuarioActual();
        } catch (Exception e) {
            return null;
        }
    }
}