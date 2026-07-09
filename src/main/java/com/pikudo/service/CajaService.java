package com.pikudo.service;

import com.pikudo.dto.caja.CajaDTO;
import com.pikudo.dto.caja.CajaResumenDTO;
import com.pikudo.dto.caja.GastoDTO;
import com.pikudo.dto.caja.MetodoPagoDTO;

import java.util.List;

public interface CajaService {

    // Control de Turnos
    CajaDTO abrirCaja(CajaDTO dto);
    CajaDTO obtenerTurnoActual();
    CajaResumenDTO obtenerResumenParaCierre(Long cajaId); // NUEVO
    CajaDTO cerrarCaja(Long cajaId, CajaDTO dto);

    // Gastos de Caja
    GastoDTO registrarGasto(GastoDTO dto);
    List<GastoDTO> listarGastosPorTurno(Long cajaId);

    // Métodos de Pago
    List<MetodoPagoDTO> listarMetodosPagoActivos();
}