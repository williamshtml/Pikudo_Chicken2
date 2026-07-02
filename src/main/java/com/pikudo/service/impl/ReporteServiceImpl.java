package com.pikudo.service.impl;

import com.pikudo.dto.reporte.ReporteDTO.*;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.GastoRepository; // Asumiendo que existe el repositorio de Gasto
import com.pikudo.service.ReporteService;
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

    @Override
    public UtilidadNetaDTO obtenerUtilidadDelDia(LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(LocalTime.MAX);

        // 1. Calcular total de ventas cobradas (Estado PAID, por ejemplo)
        BigDecimal totalVentas = pedidoRepository.calcularTotalVentasPorRango(inicioDia, finDia);
        if (totalVentas == null) totalVentas = BigDecimal.ZERO;

        // 2. Calcular total de gastos registrados en ese turno/día
        BigDecimal totalGastos = gastoRepository.calcularTotalGastosPorRango(inicioDia, finDia);
        if (totalGastos == null) totalGastos = BigDecimal.ZERO;

        // 3. Utilidad Neta = Ventas - Gastos
        BigDecimal utilidadNeta = totalVentas.subtract(totalGastos);

        return new UtilidadNetaDTO(totalVentas, totalGastos, utilidadNeta);
    }

    @Override
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDate inicio, LocalDate fin) {
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.atTime(LocalTime.MAX);
        
        // Delegamos la agrupación y ordenamiento directo a la base de datos
        return pedidoRepository.findProductosMasVendidos(desde, hasta);
    }

    @Override
    public List<FlujoHorarioDTO> obtenerFlujoHorarioPorFecha(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(LocalTime.MAX);
        
        // Agrupa los pedidos usando la función HOUR() de JPQL
        return pedidoRepository.findFlujoHorarioPorRango(desde, hasta);
    }
}