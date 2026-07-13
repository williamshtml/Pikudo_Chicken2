package com.pikudo.mapper;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.dto.comprobante.PagoDetalleDTO;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.caja.TransaccionPago;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

        // Reemplaza al antiguo c.getMetodo_pago() (String unico), que ya no existe.
        // Ahora un comprobante puede tener varios pagos (pagos divididos), asi que
        // se devuelve la lista completa para que el frontend muestre el desglose.
        List<PagoDetalleDTO> pagos = c.getPagos().stream()
                .map(this::toPagoDetalleDTO)
                .collect(Collectors.toList());
        r.setPagos(pagos);

        r.setSubtotal(c.getMontoNeto());
        r.setIgv(c.getIgv());
        r.setTotal(c.getMontoTotal());
        r.setRuc(c.getRuc());
        r.setRazonSocial(c.getRazonSocial());
        r.setTipoDocumentoCliente(c.getTipoDocumentoCliente());
        r.setNumeroDocumentoCliente(c.getNumeroDocumentoCliente());
        r.setClienteNombreSnapshot(c.getClienteNombreSnapshot());
        r.setEstadoSunat(c.getEstadoSunat() != null ? c.getEstadoSunat().name() : null);
        r.setMensajeSunat(c.getMensajeSunat());
        r.setDocumentFolderType(c.getDocumentFolderType());
        r.setFechaEmision(c.getFechaEmision());
        Pedido p = c.getPedido();
        r.setNombreCajero(p.getCajero() != null ? p.getCajero().getUsername() : "N/A");
        if ("MESA".equals(p.getTipoPedido())) {
            r.setNombreMesero(p.getMesero() != null ? p.getMesero().getUsername() : "N/A");
        } else {
            r.setNombreMesero(p.getRepartidor() != null ? "Repartidor: " + p.getRepartidor().getUsername() : "Por asignar");
        }
        return r;
    }

    private PagoDetalleDTO toPagoDetalleDTO(TransaccionPago t) {
        return new PagoDetalleDTO(t.getMetodoPago().getNombre(), t.getMonto());
    }
}
