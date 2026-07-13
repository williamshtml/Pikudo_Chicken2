package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.dto.reporte.ReporteDTO.*;
import com.pikudo.restaurant.mapper.ReporteMapper;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.GastoRepository;
import com.pikudo.restaurant.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final PedidoRepository pedidoRepository;
    private final GastoRepository gastoRepository;
    private final ReporteMapper reporteMapper; // Inyectamos el mapper

    @Override
    public UtilidadNetaDTO obtenerUtilidadDelDia(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        BigDecimal ventas = pedidoRepository.calcularTotalVentasPorRango(inicio, fin);
        BigDecimal gastos = gastoRepository.calcularTotalGastosPorRango(inicio, fin);

        // Si es null, lo tratamos como 0 antes de pasar al mapper
        return reporteMapper.toUtilidadDTO(
            ventas != null ? ventas : BigDecimal.ZERO, 
            gastos != null ? gastos : BigDecimal.ZERO
        );
    }

    // Estos métodos ya devuelven una lista de DTOs directamente desde el Repository,
    // lo cual está bien si tus JPQL ya mapean el resultado.
    @Override
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDate inicio, LocalDate fin) {
        return pedidoRepository.findProductosMasVendidos(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
    }

    @Override
    public List<FlujoHorarioDTO> obtenerFlujoHorarioPorFecha(LocalDate fecha) {
        return pedidoRepository.findFlujoHorarioPorRango(fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX));
    }
}