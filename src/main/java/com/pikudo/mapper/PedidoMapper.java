package com.pikudo.mapper;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.DetallePedido;
import com.pikudo.entity.Mesa;
import com.pikudo.entity.Usuario;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    // AHORA RECIBE AMBOS: mesero (quien atiende) y cajero (quien registra)
    public Pedido toEntity(PedidoRequestDTO dto, Mesa mesa, Usuario mesero, Usuario cajero) {
        if (dto == null) {
            return null;
        }

        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .mesero(mesero)
                .cajero(cajero) // Asignamos el cajero
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
        if (entity == null) {
            return null;
        }

        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(entity.getId());
        response.setFechaHora(entity.getFechaCreacion());
        response.setTotal(entity.getTotal());
        
        if (entity.getEstado() != null) {
            response.setEstadoPedido(entity.getEstado().name());
        }

        if (entity.getMesa() != null) {
            response.setMesaNumero(entity.getMesa().getNumero());
        }
        
        // CORRECCIÓN: Adaptado para los nuevos campos de roles
        if (entity.getMesero() != null) {
            response.setUsuarioNombre(entity.getMesero().getUsername()); // O el campo que use tu DTO
        }

        if (entity.getDetalles() != null) {
            List<PedidoResponseDTO.DetalleItemDTO> detallesDto = entity.getDetalles().stream()
                .map(this::toDetalleItemResponseDTO)
                .collect(Collectors.toList());
            response.setDetalles(detallesDto);
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