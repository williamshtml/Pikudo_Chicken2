package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.comprobante.NotaCreditoResponseDTO;
import com.pikudo.restaurant.entity.NotaCredito;
import org.springframework.stereotype.Component;

@Component
public class NotaCreditoMapper {
    public NotaCreditoResponseDTO toDTO(NotaCredito nc) {
        if (nc == null) return null;
        return NotaCreditoResponseDTO.builder()
                .id(nc.getId())
                .comprobanteId(nc.getComprobante().getId())
                .serie(nc.getSerie())
                .correlativo(nc.getCorrelativo())
                .motivo(nc.getMotivo())
                .montoDevuelto(nc.getMontoDevuelto())
                .usuarioEmisor(nc.getUsuarioEmisor() != null ? nc.getUsuarioEmisor().getUsername() : "Sistema")
                .fechaEmision(nc.getFechaEmision())
                .build();
    }
}