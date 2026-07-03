package com.pikudo.controller;

import com.pikudo.dto.caja.CajaDTO;
import com.pikudo.dto.caja.GastoDTO;
import com.pikudo.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    // 1. Abrir una nueva caja
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody CajaDTO dto) {
        return ResponseEntity.ok(cajaService.abrirCaja(dto));
    }

    // 2. Ver el estado de la caja de hoy
    @GetMapping("/turno-actual")
    public ResponseEntity<?> obtenerTurnoActual() {
        return ResponseEntity.ok(cajaService.obtenerTurnoActual());
    }

    // 3. Cerrar la caja y calcular totales
    @PutMapping("/cerrar/{id}")
    public ResponseEntity<?> cerrarCaja(@PathVariable Long id, @RequestBody CajaDTO dto) {
        return ResponseEntity.ok(cajaService.cerrarCaja(id, dto));
    }

    // 4. Registrar un gasto (comprar algo, pagar un servicio, etc.)
    @PostMapping("/gastos")
    public ResponseEntity<?> registrarGasto(@RequestBody GastoDTO dto) {
        return ResponseEntity.ok(cajaService.registrarGasto(dto));
    }

    // 5. Ver todos los gastos de un turno específico
    @GetMapping("/{id}/gastos")
    public ResponseEntity<?> listarGastosPorTurno(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.listarGastosPorTurno(id));
    }

    // 6. Ver métodos de pago (Efectivo, Yape, Plin, Tarjeta, etc.)
    @GetMapping("/metodos-pago")
    public ResponseEntity<?> listarMetodosPagoActivos() {
        return ResponseEntity.ok(cajaService.listarMetodosPagoActivos());
    }
}
