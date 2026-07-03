package com.pikudo.mapper;

import com.pikudo.dto.reporte.ReporteDTO.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ReporteMapper {

    public UtilidadNetaDTO toUtilidadDTO(BigDecimal ventas, BigDecimal gastos) {
        BigDecimal neta = ventas.subtract(gastos);
        return new UtilidadNetaDTO(ventas, gastos, neta);
    }
}