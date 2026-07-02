package com.pikudo.service.inventario.impl;

import com.pikudo.dto.inventario.InsumoDTO;
import com.pikudo.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.dto.inventario.RecetaDTO;
import com.pikudo.entity.Producto;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.inventario.Insumo;
import com.pikudo.entity.inventario.MovimientoInventario;
import com.pikudo.entity.inventario.Receta;
import com.pikudo.entity.inventario.TipoMovimiento;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.inventario.InventarioService;
import com.pikudo.repository.InsumoRepository;
import com.pikudo.repository.RecetaRepository;
import com.pikudo.repository.MovimientoInventarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioServiceImpl implements InventarioService {

    private final InsumoRepository insumoRepository;
    private final RecetaRepository recetaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsumoDTO crearInsumo(InsumoDTO dto) {
        Insumo insumo = Insumo.builder()
                .nombre(dto.getNombre())
                .stockActual(dto.getStockActual())
                .stockMinimo(dto.getStockMinimo())
                .unidadMedida(dto.getUnidadMedida())
                .estado(true)
                .build();
        return mapearInsumoADto(insumoRepository.save(insumo));
    }

    @Override
    public List<InsumoDTO> listarInsumosBajoStockMinimo() {
        return insumoRepository.findInsumosAlertaStock()
                .stream()
                .map(this::mapearInsumoADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecetaDTO registrarInsumoEnReceta(RecetaDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        Receta receta = Receta.builder()
                .producto(producto)
                .insumo(insumo)
                .cantidad(dto.getCantidad())
                .build();

        return mapearRecetaADto(recetaRepository.save(receta));
    }

    @Override
    public List<RecetaDTO> obtenerRecetaPorProducto(Long productoId) {
        return recetaRepository.findByProductoId(productoId)
                .stream()
                .map(this::mapearRecetaADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovimientoInventarioDTO registrarMovimientoManual(MovimientoInventarioDTO dto) {
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        TipoMovimiento tipo = TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase());
        
        // Modificación del Stock Físico del Insumo
        if (tipo == TipoMovimiento.INGRESO) {
            insumo.setStockActual(insumo.getStockActual().add(dto.getCantidad()));
        } else {
            insumo.setStockActual(insumo.getStockActual().subtract(dto.getCantidad()));
        }
        insumoRepository.save(insumo);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .insumo(insumo)
                .cantidad(dto.getCantidad())
                .tipoMovimiento(tipo)
                .motivo(dto.getMotivo())
                .usuario(usuario)
                .build();

        return mapearMovimientoADto(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void descontarStockPorVenta(Long productoId, Integer cantidadVendida) {
        // Buscamos todos los componentes que conforman este producto terminado
        List<Receta> recetaComponentes = recetaRepository.findByProductoId(productoId);

        for (Receta componente : recetaComponentes) {
            Insumo insumo = componente.getInsumo();
            
            // Cantidad a descontar = (Porción de la receta) * (unidades vendidas)
            BigDecimal cantidadADescontar = componente.getCantidad()
                    .multiply(BigDecimal.valueOf(cantidadVendida));

            // Restamos del stock actual
            insumo.setStockActual(insumo.getStockActual().subtract(cantidadADescontar));
            insumoRepository.save(insumo);

            // Dejamos huella en la auditoría de movimientos de manera automática
            MovimientoInventario movAuto = MovimientoInventario.builder()
                    .insumo(insumo)
                    .cantidad(cantidadADescontar)
                    .tipoMovimiento(TipoMovimiento.EGRESO)
                    .motivo("Descuento automático por Venta de Producto ID: " + productoId)
                    .build();
            movimientoRepository.save(movAuto);
        }
    }

    // --- MÉTODOS PRIVADOS DE MAPEO ---
    private InsumoDTO mapearInsumoADto(Insumo i) {
        return InsumoDTO.builder()
                .id(i.getId()).nombre(i.getNombre()).stockActual(i.getStockActual())
                .stockMinimo(i.getStockMinimo()).unidadMedida(i.getUnidadMedida()).estado(i.getEstado())
                .build();
    }

    private RecetaDTO mapearRecetaADto(Receta r) {
        return RecetaDTO.builder()
                .id(r.getId()).productoId(r.getProducto().getId()).productoNombre(r.getProducto().getNombre())
                .insumoId(r.getInsumo().getId()).insumoNombre(r.getInsumo().getNombre())
                .unidadMedida(r.getInsumo().getUnidadMedida()).cantidad(r.getCantidad())
                .build();
    }

    private MovimientoInventarioDTO mapearMovimientoADto(MovimientoInventario m) {
        return MovimientoInventarioDTO.builder()
                .id(m.getId()).insumoId(m.getInsumo().getId()).insumoNombre(m.getInsumo().getNombre())
                .cantidad(m.getCantidad()).tipoMovimiento(m.getTipoMovimiento().name()).motivo(m.getMotivo())
                .usuarioUsername(m.getUsuario() != null ? m.getUsuario().getUsername() : "SISTEMA")
                .fechaCreacion(m.getFechaCreacion())
                .build();
    }
}