package com.pikudo.controller;

import com.pikudo.dto.caja.CajaDTO;
import com.pikudo.dto.caja.GastoDTO;
import com.pikudo.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Importación agregada
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Por si Angular necesita conectarse aquí también
public class CajaController {

    private final CajaService cajaService;

    // 1. Abrir una nueva caja
    // 💵 Solo el cajero de turno abre su caja, o el admin en caso de emergencia.
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

    // 3. Cerrar la caja y calcular totales
    // 💵 Momento clave: Cuadrado de caja al finalizar el turno.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PutMapping("/cerrar/{id}")
    public ResponseEntity<?> cerrarCaja(@PathVariable Long id, @RequestBody CajaDTO dto) {
        return ResponseEntity.ok(cajaService.cerrarCaja(id, dto));
    }

    // 4. Registrar un gasto (comprar algo, pagar un servicio, etc.)
    // Si el cajero saca dinero de la caja para comprar carbón o servilletas, lo registra aquí.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/gastos")
    public ResponseEntity<?> registrarGasto(@RequestBody GastoDTO dto) {
        return ResponseEntity.ok(cajaService.registrarGasto(dto));
    }

    // 5. Ver todos los gastos de un turno específico
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/{id}/gastos")
    public ResponseEntity<?> listarGastosPorTurno(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.listarGastosPorTurno(id));
    }

    // 6. Ver métodos de pago (Efectivo, Yape, Plin, Tarjeta, etc.)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/metodos-pago")
    public ResponseEntity<?> listarMetodosPagoActivos() {
        return ResponseEntity.ok(cajaService.listarMetodosPagoActivos());
    }
}