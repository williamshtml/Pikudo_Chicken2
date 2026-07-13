package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.inventario.InsumoDTO;
import com.pikudo.restaurant.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.restaurant.dto.inventario.RecetaDTO;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.inventario.Insumo;
import com.pikudo.restaurant.entity.inventario.MovimientoInventario;
import com.pikudo.restaurant.entity.inventario.Receta;
import com.pikudo.restaurant.entity.inventario.TipoMovimiento;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    // ---- Insumo ----

    public InsumoDTO toDTO(Insumo i) {
        if (i == null) return null;
        return InsumoDTO.builder()
                .id(i.getId())
                .nombre(i.getNombre())
                .stockActual(i.getStockActual())
                .stockMinimo(i.getStockMinimo())
                .unidadMedida(i.getUnidadMedida())
                .estado(i.getEstado())
                .build();
    }

    public Insumo toEntity(InsumoDTO dto) {
        if (dto == null) return null;
        return Insumo.builder()
                .nombre(dto.getNombre())
                .stockActual(dto.getStockActual())
                .stockMinimo(dto.getStockMinimo())
                .unidadMedida(dto.getUnidadMedida())
                .estado(dto.getEstado() != null ? dto.getEstado() : true)
                .build();
    }

    // ---- MovimientoInventario ----

    public MovimientoInventarioDTO toDTO(MovimientoInventario m) {
        if (m == null) return null;
        return MovimientoInventarioDTO.builder()
                .id(m.getId())
                .insumoId(m.getInsumo().getId())
                .insumoNombre(m.getInsumo().getNombre())
                .cantidad(m.getCantidad())
                .tipoMovimiento(m.getTipoMovimiento().name())
                .motivo(m.getMotivo())
                .usuarioUsername(m.getUsuario() != null ? m.getUsuario().getUsername() : null)
                .fechaCreacion(m.getFechaCreacion())
                .build();
    }

    public MovimientoInventario toEntity(MovimientoInventarioDTO dto, Insumo insumo, Usuario usuario) {
        if (dto == null) return null;
        return MovimientoInventario.builder()
                .insumo(insumo)
                .cantidad(dto.getCantidad())
                .tipoMovimiento(TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase()))
                .motivo(dto.getMotivo())
                .usuario(usuario)
                .build();
    }

    // ---- Receta ----

    public RecetaDTO toDTO(Receta r) {
        if (r == null) return null;
        return RecetaDTO.builder()
                .id(r.getId())
                .productoId(r.getProducto().getId())
                .productoNombre(r.getProducto().getNombre())
                .insumoId(r.getInsumo().getId())
                .insumoNombre(r.getInsumo().getNombre())
                .unidadMedida(r.getInsumo().getUnidadMedida())
                .cantidad(r.getCantidad())
                .build();
    }

    public Receta toEntity(RecetaDTO dto, Producto producto, Insumo insumo) {
        if (dto == null) return null;
        return Receta.builder()
                .producto(producto)
                .insumo(insumo)
                .cantidad(dto.getCantidad())
                .build();
    }
}