package com.pikudo.controller;

import com.pikudo.dto.reporte.ReporteDTO.*;
import com.pikudo.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/utilidad")
    public ResponseEntity<UtilidadNetaDTO> obtenerUtilidadDelDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reporteService.obtenerUtilidadDelDia(fecha));
    }

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<ProductoMasVendidoDTO>> obtenerProductosMasVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(reporteService.obtenerProductosMasVendidos(inicio, fin));
    }

    @GetMapping("/flujo-horario")
    public ResponseEntity<List<FlujoHorarioDTO>> obtenerFlujoHorarioPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reporteService.obtenerFlujoHorarioPorFecha(fecha));
    }
}