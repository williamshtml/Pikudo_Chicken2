/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.service;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.repository.MesaRepository;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    // ─── CREAR PEDIDO ─────────────────────────────────────────────────────────
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        Mesa mesa = mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + dto.getMesaId()));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + dto.getUsuarioId()));

        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .usuario(usuario)
                .build();

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO item : dto.getDetalles()) {
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + item.getProductoId()));

                BigDecimal subtotal = producto.getPrecio()
                        .multiply(BigDecimal.valueOf(item.getCantidad()));

                DetallePedido detalle = DetallePedido.builder()
                        .pedido(pedido)
                        .producto(producto)
                        .cantidad(item.getCantidad())
                        .precioUnitario(producto.getPrecio())
                        .subtotal(subtotal)
                        .observaciones(item.getObservaciones())
                        .build();

                detalles.add(detalle);
                total = total.add(subtotal);
            }
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(total);

        return toDTO(pedidoRepository.save(pedido));
    }

    // ─── LISTAR TODOS ─────────────────────────────────────────────────────────
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR POR ESTADO (pantalla de cocina) ───────────────────────────────
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        return toDTO(pedido);
    }

    // ─── CAMBIAR ESTADO (PENDING → IN_KITCHEN → PAID / CANCELLED) ────────────
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        pedido.setEstado(nuevoEstado);
        return toDTO(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR ─────────────────────────────────────────────────────────────
    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("No se puede cancelar un pedido que ya fue pagado");
        }
        pedido.setEstado(EstadoPedido.CANCELLED);
        pedidoRepository.save(pedido);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private PedidoResponseDTO toDTO(Pedido p) {
        List<PedidoResponseDTO.DetalleItemDTO> detallesDTO = p.getDetalles()
                .stream()
                .map(d -> new PedidoResponseDTO.DetalleItemDTO(
                        d.getId(),
                        d.getProducto().getNombre(),
                        d.getPrecioUnitario(),
                        d.getCantidad(),
                        d.getSubtotal(),
                        d.getObservaciones()
                ))
                .collect(Collectors.toList());

        return new PedidoResponseDTO(
                p.getId(),
                p.getMesa().getNumero(),
                p.getUsuario().getUsername(),
                p.getFechaHora(),
                p.getTotal(),
                p.getEstado().name(),
                detallesDTO
        );
    }
}
