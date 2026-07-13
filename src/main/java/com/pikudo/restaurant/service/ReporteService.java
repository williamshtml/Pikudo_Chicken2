package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.reporte.ReporteDTO.*;
import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    UtilidadNetaDTO obtenerUtilidadDelDia(LocalDate fecha);
    List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDate inicio, LocalDate fin);
    List<FlujoHorarioDTO> obtenerFlujoHorarioPorFecha(LocalDate fecha);
}