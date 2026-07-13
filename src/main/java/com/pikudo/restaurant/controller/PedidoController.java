package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.dto.pedido.PedidoRequestDTO;
import com.pikudo.restaurant.dto.pedido.PedidoResponseDTO;
import com.pikudo.restaurant.entity.EstadoPedido;
import com.pikudo.restaurant.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor // <-- Adiós @Autowired, esta es la forma profesional actual
public class PedidoController {

    private final PedidoService pedidoService; // <-- Atributo final, más seguro y rápido

    // 1. CREAR UN NUEVO PEDIDO (Mesa envía comanda o entra por app)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO nuevoPedido = pedidoService.crear(dto);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    // 2. LISTAR TODOS LOS PEDIDOS
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        List<PedidoResponseDTO> pedidos = pedidoService.listarTodos();
        return ResponseEntity.ok(pedidos);
    }

    // 3. BUSCAR UN PEDIDO POR ID
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    // 4. FILTRAR PEDIDOS POR ESTADO (Ej: /api/pedidos/estado/PENDING)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO', 'MOTORIZADO')")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorEstado(@PathVariable EstadoPedido estado) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    // 5. VER PEDIDOS ACTIVOS DE UNA MESA EXCLUYENDO EL ESTADO PAGADO/CERRADO
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    @GetMapping("/mesa/{mesaId}/activos")
    public ResponseEntity<List<PedidoResponseDTO>> listarAbiertosPorMesa(
            @PathVariable Long mesaId, 
            @RequestParam(defaultValue = "PAID") EstadoPedido estadoExcluido) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarAbiertosPorMesa(mesaId, estadoExcluido);
        return ResponseEntity.ok(pedidos);
    }

    // 6. ACTUALIZAR EL ESTADO DE UN PEDIDO (Ej: Cocina termina plato o Caja procesa pago)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO', 'MOTORIZADO')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id, 
            @RequestParam EstadoPedido nuevoEstado) {
        PedidoResponseDTO pedidoActualizado = pedidoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(pedidoActualizado);
    }

    // 7. CANCELAR / ELIMINAR UN PEDIDO
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    // 8. MOTORIZADO TOMA UN PEDIDO
    @PreAuthorize("hasRole('MOTORIZADO')")
    @PutMapping("/{id}/tomar")
    public ResponseEntity<PedidoResponseDTO> tomarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedidoTomado = pedidoService.tomarPedido(id);
        return ResponseEntity.ok(pedidoTomado);
    }
}