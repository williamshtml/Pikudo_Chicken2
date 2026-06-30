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
    /*
     Traduce el RequestDTO (Frontend) a la Entidad (Base de Datos).
     La mesa y el usuario deben ser cargados previamente por el service.
     Los subtotales, precios unitarios y el total general se calculan en la lógica de negocio.
     */
    public Pedido toEntity(PedidoRequestDTO dto, Mesa mesa, Usuario usuario) {
        if (dto == null) {
            return null;
        }

        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .usuario(usuario)
                .detalles(new ArrayList<>())
                .build();

        // Si la petición contiene productos, los inicializamos en la lista de entidades
        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                DetallePedido detalle = DetallePedido.builder()
                        .cantidad(itemDto.getCantidad())
                        .observaciones(itemDto.getObservaciones())
                        .pedido(pedido) // Establece el vínculo bidireccional obligatorio
                        .build();
                pedido.getDetalles().add(detalle);
            }
        }

        return pedido;
    }

    /*
     Traduce la Entidad (Base de Datos) al ResponseDTO (Frontend).
     Mapea de forma compleja las colecciones de objetos hijos y aplana los datos compuestos.
     */
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

        // Aplanamiento de datos compuestos de objetos relacionados
        if (entity.getMesa() != null) {
            response.setMesaNumero(entity.getMesa().getNumero());
        }
        
        if (entity.getUsuario() != null) {
            response.setUsuarioNombre(entity.getUsuario().getUsername());
        }

        // Transformación compleja de la lista de filas (colección de hijos)
        if (entity.getDetalles() != null) {
            List<PedidoResponseDTO.DetalleItemDTO> detallesDto = entity.getDetalles().stream()
                .map(this::toDetalleItemResponseDTO)
                .collect(Collectors.toList());
            response.setDetalles(detallesDto);
        }

        return response;
    }

    /*
     Método privado auxiliar para mapear de forma limpia cada fila individual del pedido
     hacia la estructura interna que espera el frontend.
     */
    private PedidoResponseDTO.DetalleItemDTO toDetalleItemResponseDTO(DetallePedido detalle) {
        if (detalle == null) {
            return null;
        }

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
