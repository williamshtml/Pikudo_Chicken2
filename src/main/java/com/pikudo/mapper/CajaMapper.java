package com.pikudo.mapper;

import com.pikudo.dto.caja.CajaDTO;
import com.pikudo.dto.caja.GastoDTO;
import com.pikudo.dto.caja.MetodoPagoDTO;
import com.pikudo.entity.caja.Caja;
import com.pikudo.entity.caja.Gasto;
import com.pikudo.entity.caja.MetodoPago;
import org.springframework.stereotype.Component;

@Component
public class CajaMapper {

    public CajaDTO toCajaDTO(Caja c) {
        if (c == null) return null;
        return CajaDTO.builder()
                .id(c.getId())
                .usuarioUsername(c.getUsuario() != null ? c.getUsuario().getUsername() : null)
                .fechaApertura(c.getFechaApertura()).fechaCierre(c.getFechaCierre())
                .montoInicial(c.getMontoInicial()).montoVentasEfectivo(c.getMontoVentasEfectivo())
                .montoVentasTarjeta(c.getMontoVentasTarjeta()).montoVentasDigital(c.getMontoVentasDigital())
                .montoGastos(c.getMontoGastos()).montoFinalSistema(c.getMontoFinalSistema())
                .montoFinalReal(c.getMontoFinalReal()).observaciones(c.getObservaciones()).estado(c.getEstado())
                .build();
    }

    public GastoDTO toGastoDTO(Gasto g) {
        if (g == null) return null;
        return GastoDTO.builder()
                .id(g.getId()).cajaTurnoId(g.getCaja().getId()).monto(g.getMonto())
                .descripcion(g.getDescripcion())
                .usuarioUsername(g.getUsuario() != null ? g.getUsuario().getUsername() : "SISTEMA")
                .fechaCreacion(g.getFechaCreacion())
                .build();
    }

    public MetodoPagoDTO toMetodoPagoDTO(MetodoPago mp) {
        if (mp == null) return null;
        return MetodoPagoDTO.builder()
                .id(mp.getId()).nombre(mp.getNombre()).tipo(mp.getTipo()).activo(mp.getActivo())
                .build();
    }
}