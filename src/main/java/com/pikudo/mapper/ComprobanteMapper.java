package com.pikudo.mapper;

import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public class ComprobanteMapper {

    public ComprobanteResponseDTO toDTO(Comprobante c) {
        if (c == null) return null;

        ComprobanteResponseDTO r = new ComprobanteResponseDTO();
        r.setId(c.getId());
        r.setPedidoId(c.getPedido().getId());
        r.setTipoComprobante(c.getTipoComprobante().name());
        r.setSerie(c.getSerie());
        r.setNumeroCorrelativo(Integer.parseInt(c.getCorrelativo()));
        r.setMetodoPago(c.getMetodo_pago());
        r.setSubtotal(c.getMontoNeto());
        r.setIgv(c.getIgv());
        r.setTotal(c.getMontoTotal());
        r.setRuc(c.getRuc());
        r.setRazonSocial(c.getRazonSocial());

        Pedido p = c.getPedido();
        r.setNombreCajero(p.getCajero() != null ? p.getCajero().getUsername() : "N/A");

        if ("MESA".equals(p.getTipoPedido())) {
            r.setNombreMesero(p.getMesero() != null ? p.getMesero().getUsername() : "N/A");
        } else {
            r.setNombreMesero(p.getRepartidor() != null ? "Repartidor: " + p.getRepartidor().getUsername() : "Por asignar");
        }

        return r;
    }
}