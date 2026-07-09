package com.pikudo.mapper;

import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.Pedido;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    public PedidoResponseDTO toDTO(Pedido p) {
        if (p == null) return null;

        PedidoResponseDTO r = new PedidoResponseDTO();
        r.setId(p.getId());
        r.setMesaNumero(p.getMesa() != null ? p.getMesa().getNumero() : 0);
        r.setTotal(p.getTotal());
        r.setEstadoPedido(p.getEstado().name());
        r.setTipoPedido(p.getTipoPedido()); 
        r.setDireccion(p.getDireccion());
        r.setUrlMaps(p.getUrlMaps());
        r.setCajeroNombre(p.getCajero() != null ? p.getCajero().getUsername() : "Pendiente de Caja");
        
        // --- LÓGICA SEGURA DE RESPONSABLE ---
        // Determinamos el responsable según quién esté asignado (Mesero o Repartidor)
        if ("MESA".equals(p.getTipoPedido())) {
            r.setResponsableNombre(p.getMesero() != null ? p.getMesero().getUsername() : "N/A");
            r.setResponsableRol("Mesero");
        } else if ("DELIVERY".equals(p.getTipoPedido())) {
            if (p.getRepartidor() != null) {
                r.setResponsableNombre(p.getRepartidor().getUsername());
                r.setRepartidorId(p.getRepartidor().getId());
                r.setRepartidorTelefono(p.getRepartidor().getTelefono());
            } else {
                r.setResponsableNombre("Por asignar");
            }
            r.setResponsableRol("Repartidor");
        } else {
            r.setResponsableNombre("Mostrador");
            r.setResponsableRol("Venta Directa");
        }

        // Cálculos
        if(p.getTotal() != null){
            BigDecimal neto = p.getTotal().divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
            r.setSubtotalNeto(neto);
            r.setIgv(p.getTotal().subtract(neto));
        }

        if (p.getDetalles() != null) {
            r.setDetalles(p.getDetalles().stream().map(d -> {
                PedidoResponseDTO.DetalleItemDTO item = new PedidoResponseDTO.DetalleItemDTO();
                item.setId(d.getId());
                item.setProductoNombre(d.getProducto().getNombre());
                item.setPrecioUnitario(d.getPrecioUnitario());
                item.setCantidad(d.getCantidad());
                item.setSubtotal(d.getSubtotal());
                item.setObservaciones(d.getObservaciones());
                return item;
            }).collect(Collectors.toList()));
        }

        if (p.getFechaCreacion() != null) {
            r.setFechaCreacion(p.getFechaCreacion());
        }

        return r;
    }
}