package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.dto.mesa.MesaEstadoResponseDTO;
import com.pikudo.restaurant.dto.mesa.MesaRequestDTO;
import com.pikudo.restaurant.dto.mesa.MesaResponseDTO;
import com.pikudo.restaurant.dto.mesa.MesaSalonUpdateRequestDTO;
import com.pikudo.restaurant.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {
    private final MesaService mesaService;

    // GET /api/mesas — listado crudo (usado por el mapper que devuelve DISPONIBLE/INACTIVA)
    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(mesaService.listarTodas());
    }

    // GET /api/mesas/disponibles — solo mesas activas
    @GetMapping("/disponibles")
    public ResponseEntity<List<MesaResponseDTO>> listarDisponibles() {
        return ResponseEntity.ok(mesaService.listarDisponibles());
    }

    // GET /api/mesas/ocupacion — la que alimenta el panel visual (MesaPage.tsx)
    @GetMapping("/ocupacion")
    public ResponseEntity<List<MesaEstadoResponseDTO>> listarConOcupacion() {
        return ResponseEntity.ok(mesaService.listarConOcupacion());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.buscarPorId(id));
    }

    // POST /api/mesas — antes no existía, causaba el 500/405 al "Crear Mesa"
    @PostMapping
    public ResponseEntity<MesaResponseDTO> crear(@Valid @RequestBody MesaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaService.crear(dto));
    }

    // PUT /api/mesas/{id} — editar número/capacidad/salón de una mesa existente
    @PutMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody MesaRequestDTO dto) {
        return ResponseEntity.ok(mesaService.actualizar(id, dto));
    }

    // PATCH /api/mesas/{id}/salon — reasignar rápidamente el salón de una mesa
    // (usado por el modo "Asignar salón" del panel de mesas, caja y tablets de mesero)
    @PatchMapping("/{id}/salon")
    public ResponseEntity<MesaResponseDTO> actualizarSalon(
            @PathVariable Long id,
            @Valid @RequestBody MesaSalonUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(mesaService.actualizarSalon(id, dto.getSalon()));
    }

    // DELETE /api/mesas/{id} — antes no existía. Es baja lógica (mesa.estado = false),
    // no borrado físico, y falla con 400 si la mesa tiene un pedido abierto.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        mesaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}