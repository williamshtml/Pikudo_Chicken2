package com.pikudo.mapper;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.*;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    public Pedido toEntity(PedidoRequestDTO dto, Mesa mesa, Usuario mesero, Usuario cajero) {
        if (dto == null) return null;

        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .mesero(mesero)
                .cajero(cajero)
                .estado(EstadoPedido.PENDING)
                .tipoPedido(mesa == null ? "DELIVERY" : "MESA")
                .detalles(new ArrayList<>())
                .build();

        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                DetallePedido detalle = DetallePedido.builder()
                        .cantidad(itemDto.getCantidad())
                        .observaciones(itemDto.getObservaciones())
                        .pedido(pedido)
                        .build();
                pedido.getDetalles().add(detalle);
            }
        }
        return pedido;
    }

    public PedidoResponseDTO toResponseDTO(Pedido entity) {
        if (entity == null) return null;

        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(entity.getId());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setTotal(entity.getTotal());
        
        if (entity.getEstado() != null) response.setEstadoPedido(entity.getEstado().name());
        if (entity.getMesa() != null) response.setMesaNumero(entity.getMesa().getNumero());

        // --- TRAZABILIDAD INTELIGENTE ---
        
        // 1. CAJA: Siempre se registra quién cobró o quién gestionó
        response.setCajeroNombre(entity.getCajero() != null ? entity.getCajero().getUsername() : "Pendiente de Caja");

        // 2. RESPONSABLE: Dinámico según tipo de pedido
        if ("MESA".equals(entity.getTipoPedido())) {
            response.setResponsableNombre(entity.getMesero() != null ? entity.getMesero().getUsername() : "N/A");
            response.setResponsableRol("Mesero");
            response.setUsuarioNombre(response.getResponsableNombre()); // Compatibilidad front
        } else if ("DELIVERY".equals(entity.getTipoPedido())) {
            response.setResponsableNombre(entity.getRepartidor() != null ? entity.getRepartidor().getUsername() : "Por asignar");
            response.setResponsableRol("Repartidor");
            response.setUsuarioNombre(response.getResponsableNombre());
        } else {
            response.setResponsableNombre("Mostrador");
            response.setResponsableRol("Venta Directa");
            response.setUsuarioNombre(response.getResponsableNombre());
        }

        if (entity.getDetalles() != null) {
            response.setDetalles(entity.getDetalles().stream()
                    .map(this::toDetalleItemResponseDTO)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private PedidoResponseDTO.DetalleItemDTO toDetalleItemResponseDTO(DetallePedido detalle) {
        if (detalle == null) return null;

        PedidoResponseDTO.DetalleItemDTO itemDto = new PedidoResponseDTO.DetalleItemDTO();
        itemDto.setId(detalle.getId());
        itemDto.setCantidad(detalle.getCantidad());
        itemDto.setPrecioUnitario(detalle.getPrecioUnitario());
        itemDto.setSubtotal(detalle.getSubtotal());
        itemDto.setObservaciones(detalle.getObservaciones());

        if (detalle.getProducto() != null) {
            itemDto.setProductoNombre(detalle.getProducto().getNombre());
        }
        return itemDto;
    }
}