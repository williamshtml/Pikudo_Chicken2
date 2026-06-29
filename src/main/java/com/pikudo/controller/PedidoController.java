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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*") // Permite la conexión con Angular sin problemas de CORS
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // 1. CREAR UN NUEVO PEDIDO (Mesa envía comanda)
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO nuevoPedido = pedidoService.crear(dto);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    // 2. LISTAR TODOS LOS PEDIDOS
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        List<PedidoResponseDTO> pedidos = pedidoService.listarTodos();
        return ResponseEntity.ok(pedidos);
    }

    // 3. BUSCAR UN PEDIDO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    // 4. FILTRAR PEDIDOS POR ESTADO (Ej: /api/pedidos/estado/PENDING)
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorEstado(@PathVariable EstadoPedido estado) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    // 5. VER PEDIDOS ACTIVOS DE UNA MESA EXCLUYENDO EL ESTADO PAGADO/CERRADO
    @GetMapping("/mesa/{mesaId}/activos")
    public ResponseEntity<List<PedidoResponseDTO>> listarAbiertosPorMesa(
            @PathVariable Long mesaId, 
            @RequestParam(defaultValue = "PAID") EstadoPedido estadoExcluido) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarAbiertosPorMesa(mesaId, estadoExcluido);
        return ResponseEntity.ok(pedidos);
    }

    // 6. ACTUALIZAR EL ESTADO DE UN PEDIDO (Ej: Cocina termina plato o Caja procesa pago)
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id, 
            @RequestParam EstadoPedido nuevoEstado) {
        PedidoResponseDTO pedidoActualizado = pedidoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(pedidoActualizado);
    }

    // 7. CANCELAR / ELIMINAR UN PEDIDO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}