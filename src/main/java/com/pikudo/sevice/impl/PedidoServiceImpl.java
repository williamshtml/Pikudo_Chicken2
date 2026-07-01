package com.pikudo.sevice.impl;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.repository.*;
import com.pikudo.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MesaRepository mesaRepository;
    @Autowired private SimpMessagingTemplate template;

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        Mesa mesa = mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + dto.getMesaId()));
        
        Usuario mesero = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setMesero(mesero); // Correcto: usamos el campo de la entidad
        pedido.setFechaHora(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDING);
        
        BigDecimal totalAcumulado = BigDecimal.ZERO;
        List<DetallePedido> detallesEntidad = new ArrayList<>();

        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                Producto producto = productoRepository.findById(itemDto.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                
                DetallePedido detalle = new DetallePedido();
                detalle.setPedido(pedido);
                detalle.setProducto(producto);
                detalle.setCantidad(itemDto.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setObservaciones(itemDto.getObservaciones());
                detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad())));
                
                totalAcumulado = totalAcumulado.add(detalle.getSubtotal());
                detallesEntidad.add(detalle);
            }
        }
        
        pedido.setTotal(totalAcumulado); 
        pedido.setDetalles(detallesEntidad);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        PedidoResponseDTO response = mapearADto(pedidoGuardado);
        template.convertAndSend("/topic/pedidos", response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        return mapearAListas(pedidoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return mapearAListas(pedidoRepository.findByEstado(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido) {
        return mapearAListas(pedidoRepository.findByMesaIdAndEstadoNot(mesaId, estadoExcluido));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        return mapearADto(pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado")));
    }

    @Override
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();
        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);
        PedidoResponseDTO response = mapearADto(actualizado);
        template.convertAndSend("/topic/pedidos", response);
        return response;
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        pedidoRepository.deleteById(id);
    }

    private List<PedidoResponseDTO> mapearAListas(List<Pedido> pedidos) {
        List<PedidoResponseDTO> lista = new ArrayList<>();
        for (Pedido p : pedidos) lista.add(mapearADto(p));
        return lista;
    }

    private PedidoResponseDTO mapearADto(Pedido p) {
        PedidoResponseDTO r = new PedidoResponseDTO();
        r.setId(p.getId());
        r.setMesaNumero(p.getMesa().getNumero());
        r.setUsuarioNombre(p.getMesero() != null ? p.getMesero().getUsername() : "Sin asignar");
        r.setFechaHora(p.getFechaHora());
        r.setEstadoPedido(p.getEstado().name());
        r.setTotal(p.getTotal());
        
        BigDecimal neto = p.getTotal().divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        r.setSubtotalNeto(neto);
        r.setIgv(p.getTotal().subtract(neto));

        List<PedidoResponseDTO.DetalleItemDTO> detalles = new ArrayList<>();
        for (DetallePedido d : p.getDetalles()) {
            PedidoResponseDTO.DetalleItemDTO item = new PedidoResponseDTO.DetalleItemDTO();
            item.setId(d.getId());
            item.setProductoNombre(d.getProducto().getNombre());
            item.setPrecioUnitario(d.getPrecioUnitario());
            item.setCantidad(d.getCantidad());
            item.setSubtotal(d.getSubtotal());
            item.setObservaciones(d.getObservaciones());
            detalles.add(item);
        }
        r.setDetalles(detalles);
        return r;
    }
}