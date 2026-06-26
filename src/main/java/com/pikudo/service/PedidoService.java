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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        // fechaHora, total y estado usan sus valores @Builder.Default (now(), ZERO, PENDING)

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        for (PedidoRequestDTO.DetalleItemDTO item : dto.getDetalles()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }

            // Se descuenta el stock al momento de crear el pedido
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .observaciones(item.getObservaciones())
                    .build();

            detalles.add(detalle);
            totalPedido = totalPedido.add(subtotal);
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(totalPedido);

        Pedido guardado = pedidoRepository.save(pedido);
        return toDTO(guardado);
    }

    // ─── LISTAR TODOS ─────────────────────────────────────────────────────────
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR POR ESTADO (ej: pantalla de cocina = IN_KITCHEN) ──────────────
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR PEDIDOS ABIERTOS DE UNA MESA (distintos de un estado dado) ────
    public List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido) {
        return pedidoRepository.findByMesaIdAndEstadoNot(mesaId, estadoExcluido)
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

    // ─── CAMBIAR ESTADO (ej: PENDING → IN_KITCHEN → PAID / CANCELLED) ─────────
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        pedido.setEstado(nuevoEstado);
        return toDTO(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR (repone el stock de los productos del pedido) ───────────────
    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("No se puede cancelar un pedido que ya fue pagado");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setEstado(EstadoPedido.CANCELLED);
        pedidoRepository.save(pedido);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private PedidoResponseDTO toDTO(Pedido p) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(p.getId());
        response.setMesaNumero(p.getMesa() != null ? p.getMesa().getNumero() : null);
        response.setUsuarioNombre(p.getUsuario() != null ? p.getUsuario().getUsername() : null);
        response.setFechaHora(p.getFechaHora());
        response.setTotal(p.getTotal());
        response.setEstadoPedido(p.getEstado() != null ? p.getEstado().name() : null);

        List<PedidoResponseDTO.DetalleItemDTO> detallesDTO = p.getDetalles() == null
                ? new ArrayList<>()
                : p.getDetalles().stream().map(this::toDetalleDTO).collect(Collectors.toList());
        response.setDetalles(detallesDTO);

        return response;
    }

    private PedidoResponseDTO.DetalleItemDTO toDetalleDTO(DetallePedido d) {
        PedidoResponseDTO.DetalleItemDTO item = new PedidoResponseDTO.DetalleItemDTO();
        item.setId(d.getId());
        item.setProductoNombre(d.getProducto() != null ? d.getProducto().getNombre() : null);
        item.setPrecioUnitario(d.getPrecioUnitario());
        item.setCantidad(d.getCantidad());
        item.setSubtotal(d.getSubtotal());
        item.setObservaciones(d.getObservaciones());
        return item;
    }
}