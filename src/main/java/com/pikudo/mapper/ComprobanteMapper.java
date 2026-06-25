package com.pikudo.mapper;

import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.TipoComprobante;
import org.springframework.stereotype.Component;

@Component

public class ComprobanteMapper {
    /*
     Traduce el RequestDTO (Frontend) a la Entidad (Base de Datos).
     Requiere el objeto Pedido correspondiente resuelto desde la capa de servicios.
     Los montos desglosados y correlativos los calcula el Service al procesar el cierre financiero.
     */
    public Comprobante toEntity(ComprobanteRequestDTO dto, Pedido pedido) {
        if (dto == null) {
            return null;
        }

        return Comprobante.builder()
                .pedido(pedido)
                .tipoComprobante(dto.getTipoComprobante() != null ? TipoComprobante.valueOf(dto.getTipoComprobante().toUpperCase()) : null)
                .metodo_pago(dto.getMetodoPago())
                .ruc(dto.getRuc())
                .razonSocial(dto.getRazonSocial())
                .build();
    }

    /*
     Traduce la Entidad (Base de Datos) al ResponseDTO (Frontend).
     Mapea los montos desglosados y transforma los tipos de datos requeridos por el frontend.
     */
    public ComprobanteResponseDTO toResponseDTO(Comprobante entity) {
        if (entity == null) {
            return null;
        }

        ComprobanteResponseDTO response = new ComprobanteResponseDTO();
        response.setId(entity.getId());
        
        if (entity.getPedido() != null) {
            response.setPedidoId(entity.getPedido().getId());
        }
        
        if (entity.getTipoComprobante() != null) {
            response.setTipoComprobante(entity.getTipoComprobante().name());
        }
        
        response.setSerie(entity.getSerie());
        response.setMetodoPago(entity.getMetodo_pago());
        
        // Mapeo y traducción de nombres de montos
        response.setSubtotal(entity.getMontoNeto());
        response.setIgv(entity.getIgv());
        response.setTotal(entity.getMontoTotal());
        
        response.setRuc(entity.getRuc());
        response.setRazonSocial(entity.getRazonSocial());
        response.setFechaEmision(entity.getFechaEmision());

        // Traducción de tipo de dato: Convierte el correlativo String de la BD a Integer para el frontend
        if (entity.getCorrelativo() != null) {
            try {
                response.setNumeroCorrelativo(Integer.parseInt(entity.getCorrelativo()));
            } catch (NumberFormatException e) {
                response.setNumeroCorrelativo(null); // Resguardo por si el formato en BD contiene letras
            }
        }

        return response;
    }
}
