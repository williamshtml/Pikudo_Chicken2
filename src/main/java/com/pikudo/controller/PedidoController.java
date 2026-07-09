package com.pikudo.controller;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.EstadoPedido;
import com.pikudo.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Importación agregada
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
 
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // 1. CREAR UN NUEVO PEDIDO (Mesa envía comanda)
    // El mozo anota, el cajero puede tomar pedido por teléfono y el admin manda.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO nuevoPedido = pedidoService.crear(dto);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    // 2. LISTAR TODOS LOS PEDIDOS
    // Todos menos el motorizado necesitan ver los pedidos en general.
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
    // Acá sumamos al motorizado por si necesita ver los pedidos en estado "EN_CAMINO".
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
    // El motorizado necesita esto para marcar que ya entregó la comida.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO', 'MOTORIZADO')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id, 
            @RequestParam EstadoPedido nuevoEstado) {
        PedidoResponseDTO pedidoActualizado = pedidoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(pedidoActualizado);
    }

    // 7. CANCELAR / ELIMINAR UN PEDIDO
    // Ojo acá: solo el Admin y el Cajero pueden anular pedidos. Bloqueamos al mozo por seguridad.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}