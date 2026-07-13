package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.dto.caja.CajaDTO;
import com.pikudo.restaurant.dto.caja.CajaResumenDTO;
import com.pikudo.restaurant.dto.caja.GastoDTO;
import com.pikudo.restaurant.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor

public class CajaController {

    private final CajaService cajaService;

    // 1. Abrir una nueva caja
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody CajaDTO dto) {
        return ResponseEntity.ok(cajaService.abrirCaja(dto));
    }

    // 2. Ver el estado de la caja de hoy
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/turno-actual")
    public ResponseEntity<?> obtenerTurnoActual() {
        return ResponseEntity.ok(cajaService.obtenerTurnoActual());
    }

    // 3. NUEVO: Ver el resumen ANTES de cerrar (para que el cajero cuente el efectivo)
    // 💵 El cajero consulta esto primero, cuenta físicamente el dinero,
    // y luego manda el conteo real en el PUT /cerrar/{id}
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/{id}/resumen-cierre")
    public ResponseEntity<?> obtenerResumenParaCierre(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.obtenerResumenParaCierre(id));
    }

    // 4. Cerrar la caja y calcular totales
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PutMapping("/cerrar/{id}")
    public ResponseEntity<?> cerrarCaja(@PathVariable Long id, @RequestBody CajaDTO dto) {
        return ResponseEntity.ok(cajaService.cerrarCaja(id, dto));
    }

    // 5. Registrar un gasto
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/gastos")
    public ResponseEntity<?> registrarGasto(@RequestBody GastoDTO dto) {
        return ResponseEntity.ok(cajaService.registrarGasto(dto));
    }

    // 6. Ver todos los gastos de un turno específico
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/{id}/gastos")
    public ResponseEntity<?> listarGastosPorTurno(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.listarGastosPorTurno(id));
    }

    // 7. Ver métodos de pago (Efectivo, Yape, Plin, Tarjeta, etc.)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/metodos-pago")
    public ResponseEntity<?> listarMetodosPagoActivos() {
        return ResponseEntity.ok(cajaService.listarMetodosPagoActivos());
    }
}