package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.dto.inventario.InsumoDTO;
import com.pikudo.restaurant.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.restaurant.dto.inventario.RecetaDTO;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.inventario.Insumo;
import com.pikudo.restaurant.entity.inventario.MovimientoInventario;
import com.pikudo.restaurant.entity.inventario.Receta;
import com.pikudo.restaurant.entity.inventario.TipoMovimiento;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.mapper.InventarioMapper;
import com.pikudo.restaurant.repository.InsumoRepository;
import com.pikudo.restaurant.repository.MovimientoInventarioRepository;
import com.pikudo.restaurant.repository.ProductoRepository;
import com.pikudo.restaurant.repository.RecetaRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.service.InventarioService;
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
        return inventarioMapper.toDTO(movimientoRepository.save(movimiento));
    }

    @Override
    public void descontarStockPorVenta(Long productoId, Integer cantidadVendida) {
        List<Receta> receta = recetaRepository.findByProductoId(productoId);

        if (receta.isEmpty()) {
            // No todos los productos tienen receta configurada (ej. bebidas envasadas);
            // no es un error, simplemente no hay nada que descontar
            return;
        }

        Usuario usuario = usuarioActualOSistema();

        for (Receta r : receta) {
            Insumo insumo = r.getInsumo();
            BigDecimal cantidadADescontar = r.getCantidad().multiply(BigDecimal.valueOf(cantidadVendida));

            aplicarMovimientoAlStock(insumo, cantidadADescontar, TipoMovimiento.EGRESO);

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

    @Override
    public void revertirStockPorCancelacion(Long productoId, Integer cantidadCancelada) {
        List<Receta> receta = recetaRepository.findByProductoId(productoId);

        if (receta.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioActualOSistema();

        for (Receta r : receta) {
            Insumo insumo = r.getInsumo();
            BigDecimal cantidadADevolver = r.getCantidad().multiply(BigDecimal.valueOf(cantidadCancelada));

            aplicarMovimientoAlStock(insumo, cantidadADevolver, TipoMovimiento.INGRESO);

            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .insumo(insumo)
                    .cantidad(cantidadADevolver)
                    .tipoMovimiento(TipoMovimiento.INGRESO)
                    .motivo("Reversión por cancelación - Producto #" + productoId)
                    .usuario(usuario)
                    .build();
            movimientoRepository.save(movimiento);
        }
    }

    // ---- Helpers privados ----

    private void aplicarMovimientoAlStock(Insumo insumo, BigDecimal cantidad, TipoMovimiento tipo) {
        int filasActualizadas;
        if (tipo == TipoMovimiento.INGRESO) {
            filasActualizadas = insumoRepository.incrementarStockAtomico(insumo.getId(), cantidad);
        } else {
            filasActualizadas = insumoRepository.descontarStockAtomico(insumo.getId(), cantidad);
        }

        if (filasActualizadas == 0) {
            throw new ResourceNotFoundException("No se pudo actualizar el stock del insumo: " + insumo.getId());
        }

        // Refrescamos el objeto en memoria con el valor real ya actualizado en BD
        insumoRepository.findById(insumo.getId()).ifPresent(actualizado ->
                insumo.setStockActual(actualizado.getStockActual())
        );

        if (tipo != TipoMovimiento.INGRESO && insumo.getStockActual().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Stock negativo para '{}': quedó en {} {}",
                    insumo.getNombre(), insumo.getStockActual(), insumo.getUnidadMedida());
        }
    }

    private Usuario usuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesión no encontrado"));
    }

    private Usuario usuarioActualOSistema() {
        try {
            return usuarioActual();
        } catch (Exception e) {
            return null;
        }
    }
}