package com.pikudo.service.impl;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.exception.BusinessException;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.mapper.PedidoMapper;
import com.pikudo.repository.*;
import com.pikudo.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final SimpMessagingTemplate template;
    private final TicketPrinterServiceImpl ticketPrinterService;
    private final ImpresoraRepository impresoraRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario mesero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesión no encontrado"));

        Mesa mesa = (dto.getMesaId() != null) ? mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con ID: " + dto.getMesaId())) : null;

        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setMesero(mesero);

        String tipo = (dto.getTipoPedido() != null) ? dto.getTipoPedido().toUpperCase() : "MESA";
        pedido.setTipoPedido(tipo);

        if ("DELIVERY".equals(tipo)) {
            pedido.setEstado(EstadoPedido.PENDING);
            pedido.setDireccion(dto.getDireccion());
            if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
                String urlFormateada = "https://www.google.com/maps/search/?api=1&query="
                        + URLEncoder.encode(dto.getDireccion(), StandardCharsets.UTF_8);
                pedido.setUrlMaps(urlFormateada);
            }
        } else {
            pedido.setEstado(EstadoPedido.PENDING);
        }

        BigDecimal totalAcumulado = BigDecimal.ZERO;
        List<DetallePedido> detallesEntidad = new ArrayList<>();
        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                Producto producto = productoRepository.findById(itemDto.getProductoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + itemDto.getProductoId()));
                DetallePedido detalle = new DetallePedido();
                detalle.setPedido(pedido);
                detalle.setProducto(producto);
                detalle.setCantidad(itemDto.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad())));
                totalAcumulado = totalAcumulado.add(detalle.getSubtotal());
                detallesEntidad.add(detalle);
            }
        }
        pedido.setTotal(totalAcumulado);
        pedido.setDetalles(detallesEntidad);

        Pedido guardado = pedidoRepository.save(pedido);
        ticketPrinterService.imprimirTicketsPorArea(guardado);
        PedidoResponseDTO response = pedidoMapper.toDTO(guardado);

        // Disparamos al canal general (esto después lo filtramos por zona si crece mucho)
        if ("DELIVERY".equals(guardado.getTipoPedido())) {
            template.convertAndSend("/topic/repartidores", response);
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO tomarPedido(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario motorizado = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // Validar que nadie más lo haya tomado ya (o que no esté cancelado)
        if (pedido.getRepartidor() != null) {
            throw new BusinessException("Este pedido ya fue asignado a otro motorizado.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELLED) {
            throw new BusinessException("No puedes tomar un pedido cancelado.");
        }
        
        pedido.setRepartidor(motorizado);
        // Cuando lo toma, significa que ya va a salir con él
        pedido.setEstado(EstadoPedido.ON_DELIVERY); 
        
        Pedido guardado = pedidoRepository.save(pedido);
        PedidoResponseDTO response = pedidoMapper.toDTO(guardado);
        
        // Avisar a todos que este pedido ya cambió de estado (para que desaparezca de la lista de disponibles)
        template.convertAndSend("/topic/pedidos", response);
        
        return response;
    }

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll().stream().map(pedidoMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream().map(pedidoMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido) {
        return pedidoRepository.findByMesaIdAndEstadoNot(mesaId, estadoExcluido).stream().map(pedidoMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        return pedidoMapper.toDTO(pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // REGLA ESTRICTA DE REPARTIDORES
        if (user.getRol() != null && "MOTORIZADO".equals(user.getRol().getNombre().name())) {
            if (pedido.getRepartidor() == null || !pedido.getRepartidor().getUsername().equals(username)) {
                throw new BusinessException("No puedes modificar un pedido que no tienes asignado.");
            }
            if (nuevoEstado == EstadoPedido.DELIVERED && pedido.getEstado() != EstadoPedido.ON_DELIVERY) {
                throw new BusinessException("Debes estar en camino (ON_DELIVERY) para poder marcarlo como entregado.");
            }
        }

        pedido.setEstado(nuevoEstado);
        
        Pedido actualizado = pedidoRepository.save(pedido);
        PedidoResponseDTO response = pedidoMapper.toDTO(actualizado);
        
        template.convertAndSend("/topic/pedidos", response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelar(Long id) {
        Pedido p = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        p.setEstado(EstadoPedido.CANCELLED);
        
        Pedido actualizado = pedidoRepository.save(p);
        template.convertAndSend("/topic/pedidos", pedidoMapper.toDTO(actualizado));
    }
}