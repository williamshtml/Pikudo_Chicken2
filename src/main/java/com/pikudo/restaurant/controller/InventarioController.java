package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.dto.inventario.InsumoDTO;
import com.pikudo.restaurant.dto.inventario.MovimientoInventarioDTO;
import com.pikudo.restaurant.dto.inventario.RecetaDTO;
import com.pikudo.restaurant.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping("/insumos")
    public ResponseEntity<InsumoDTO> crearInsumo(@Valid @RequestBody InsumoDTO dto) {
        return ResponseEntity.ok(inventarioService.crearInsumo(dto));
    }

    @GetMapping("/insumos/bajo-stock")
    public ResponseEntity<List<InsumoDTO>> listarBajoStockMinimo() {
        return ResponseEntity.ok(inventarioService.listarInsumosBajoStockMinimo());
    }

    @PostMapping("/recetas")
    public ResponseEntity<RecetaDTO> registrarInsumoEnReceta(@Valid @RequestBody RecetaDTO dto) {
        return ResponseEntity.ok(inventarioService.registrarInsumoEnReceta(dto));
    }

    @GetMapping("/recetas/producto/{productoId}")
    public ResponseEntity<List<RecetaDTO>> obtenerRecetaPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.obtenerRecetaPorProducto(productoId));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventarioDTO> registrarMovimientoManual(@Valid @RequestBody MovimientoInventarioDTO dto) {
        return ResponseEntity.ok(inventarioService.registrarMovimientoManual(dto));
    }
}