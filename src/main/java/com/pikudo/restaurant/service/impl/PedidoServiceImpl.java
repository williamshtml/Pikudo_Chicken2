package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.dto.pedido.PedidoRequestDTO;
import com.pikudo.restaurant.dto.pedido.PedidoResponseDTO;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.EstadoPedido;
import com.pikudo.restaurant.entity.Mesa;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.catalog.ProductoVariante;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderServiceType;
import com.pikudo.restaurant.entity.orders.OrderSource;
import com.pikudo.restaurant.entity.orders.TableSession;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.mapper.PedidoMapper;
import com.pikudo.restaurant.repository.MesaRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.ProductoRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.repository.catalog.ProductoVarianteRepository;
import com.pikudo.restaurant.service.PedidoService;
import com.pikudo.restaurant.service.orders.TableSessionService;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private static final List<OrderOperationalStatus> TERMINAL_STATUSES = List.of(
            OrderOperationalStatus.REJECTED,
            OrderOperationalStatus.DELIVERED,
            OrderOperationalStatus.CANCELLED
    );

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final SimpMessagingTemplate template;
    private final TicketPrinterServiceImpl ticketPrinterService;
    private final PedidoMapper pedidoMapper;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final TableSessionService tableSessionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario mesero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesion no encontrado"));

        Mesa mesa = dto.getMesaId() != null ? mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con ID: " + dto.getMesaId())) : null;

        String tipo = dto.getTipoPedido() != null ? dto.getTipoPedido().toUpperCase() : "MESA";
        OrderServiceType serviceType = toServiceType(tipo);

        Pedido pedido = new Pedido();
        pedido.setOrderCode(generateCode("ORD"));
        pedido.setMesa(mesa);
        pedido.setMesero(mesero);
        pedido.setTipoPedido(tipo);
        pedido.setServiceType(serviceType);
        pedido.setSource(toSource(serviceType));
        pedido.setEstado(EstadoPedido.PENDING);
        pedido.setEstadoOperativo(OrderOperationalStatus.UNREAD);
        pedido.setEstadoPago(OrderPaymentStatus.UNPAID);
        pedido.setDiscountTotal(BigDecimal.ZERO);
        pedido.setTaxTotal(BigDecimal.ZERO);
        pedido.setDeliveryFee(BigDecimal.ZERO);

        if (serviceType == OrderServiceType.DINE_IN && mesa != null) {
            TableSession session = tableSessionService.ensureOpenSession(mesa.getId(), null, mesero);
            pedido.setTableSession(session);
        }

        if (serviceType == OrderServiceType.DELIVERY) {
            pedido.setTrackingCode(generateCode("TRK"));
            pedido.setDireccion(dto.getDireccion());
            if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
                pedido.setUrlMaps("https://www.google.com/maps/search/?api=1&query="
                        + URLEncoder.encode(dto.getDireccion(), StandardCharsets.UTF_8));
            }
        }

        BigDecimal totalAcumulado = BigDecimal.ZERO;
        List<DetallePedido> detallesEntidad = new ArrayList<>();
        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                Producto producto = productoRepository.findById(itemDto.getProductoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + itemDto.getProductoId()));
                ProductoVariante variante = productoVarianteRepository.findByProductoIdOrderByOrdenAscIdAsc(producto.getId())
                        .stream()
                        .findFirst()
                        .orElse(null);

                BigDecimal lineTotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad()));
                DetallePedido detalle = DetallePedido.builder()
                        .pedido(pedido)
                        .producto(producto)
                        .variante(variante)
                        .cantidad(itemDto.getCantidad())
                        .precioUnitario(producto.getPrecio())
                        .precioUnitarioSnapshot(producto.getPrecio())
                        .productoNombreSnapshot(producto.getNombre())
                        .varianteNombreSnapshot(variante != null ? variante.getNombre() : producto.getNombre())
                        .subtotal(lineTotal)
                        .discountAmount(BigDecimal.ZERO)
                        .taxAmount(BigDecimal.ZERO)
                        .lineTotal(lineTotal)
                        .observaciones(itemDto.getObservaciones())
                        .build();
                totalAcumulado = totalAcumulado.add(lineTotal);
                detallesEntidad.add(detalle);
            }
        }
        pedido.setSubtotal(totalAcumulado);
        pedido.setTotal(totalAcumulado);
        pedido.setDetalles(detallesEntidad);

        Pedido guardado = pedidoRepository.save(pedido);
        ticketPrinterService.imprimirTicketsPorArea(guardado);
        PedidoResponseDTO response = pedidoMapper.toDTO(guardado);

        if (serviceType == OrderServiceType.DELIVERY) {
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

        if (pedido.getRepartidor() != null) {
            throw new BusinessException("Este pedido ya fue asignado a otro motorizado.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELLED) {
            throw new BusinessException("No puedes tomar un pedido cancelado.");
        }

        pedido.setRepartidor(motorizado);
        pedido.setEstado(EstadoPedido.ON_DELIVERY);
        pedido.setEstadoOperativo(OrderOperationalStatus.ON_DELIVERY);

        Pedido guardado = pedidoRepository.save(pedido);
        PedidoResponseDTO response = pedidoMapper.toDTO(guardado);
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
        return pedidoRepository.findByMesaIdAndEstadoOperativoNotIn(mesaId, TERMINAL_STATUSES).stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList());
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

        if (user.getRol() != null && "MOTORIZADO".equals(user.getRol().getNombre().name())) {
            if (pedido.getRepartidor() == null || !pedido.getRepartidor().getUsername().equals(username)) {
                throw new BusinessException("No puedes modificar un pedido que no tienes asignado.");
            }
            if (nuevoEstado == EstadoPedido.DELIVERED && pedido.getEstado() != EstadoPedido.ON_DELIVERY) {
                throw new BusinessException("Debes estar en camino (ON_DELIVERY) para poder marcarlo como entregado.");
            }
        }

        pedido.setEstado(nuevoEstado);
        pedido.setEstadoOperativo(toOperationalStatus(nuevoEstado));
        if (nuevoEstado == EstadoPedido.PAID) {
            pedido.setEstadoPago(OrderPaymentStatus.PAID);
        } else if (nuevoEstado == EstadoPedido.CANCELLED && pedido.getEstadoPago() != OrderPaymentStatus.PAID) {
            pedido.setEstadoPago(OrderPaymentStatus.VOIDED);
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        if (actualizado.getEstadoOperativo() == OrderOperationalStatus.CANCELLED) {
            tableSessionService.closeIfNoOpenOrders(actualizado.getTableSession(), user);
        }
        PedidoResponseDTO response = pedidoMapper.toDTO(actualizado);
        template.convertAndSend("/topic/pedidos", response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        pedido.setEstado(EstadoPedido.CANCELLED);
        pedido.setEstadoOperativo(OrderOperationalStatus.CANCELLED);
        if (pedido.getEstadoPago() != OrderPaymentStatus.PAID) {
            pedido.setEstadoPago(OrderPaymentStatus.VOIDED);
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        tableSessionService.closeIfNoOpenOrders(actualizado.getTableSession(), null);
        template.convertAndSend("/topic/pedidos", pedidoMapper.toDTO(actualizado));
    }

    private String generateCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private OrderServiceType toServiceType(String tipoPedido) {
        if ("DELIVERY".equals(tipoPedido)) {
            return OrderServiceType.DELIVERY;
        }
        if ("RECOJO".equals(tipoPedido) || "PICKUP".equals(tipoPedido)) {
            return OrderServiceType.PICKUP;
        }
        return OrderServiceType.DINE_IN;
    }

    private OrderSource toSource(OrderServiceType serviceType) {
        return switch (serviceType) {
            case DELIVERY -> OrderSource.WEB;
            case PICKUP -> OrderSource.WALK_IN;
            case DINE_IN -> OrderSource.DINE_IN;
        };
    }

    private OrderOperationalStatus toOperationalStatus(EstadoPedido estado) {
        return switch (estado) {
            case ON_DELIVERY -> OrderOperationalStatus.ON_DELIVERY;
            case DELIVERED, PAID -> OrderOperationalStatus.DELIVERED;
            case CANCELLED -> OrderOperationalStatus.CANCELLED;
            case PENDING -> OrderOperationalStatus.UNREAD;
        };
    }
}
