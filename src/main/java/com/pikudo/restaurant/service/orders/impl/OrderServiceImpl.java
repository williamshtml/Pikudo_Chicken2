package com.pikudo.restaurant.service.orders.impl;

import com.pikudo.restaurant.dto.orders.OrderCreateRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderStatusChangeRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderStatusHistoryResponseDTO;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.EstadoPedido;
import com.pikudo.restaurant.entity.Mesa;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.catalog.Modifier;
import com.pikudo.restaurant.entity.catalog.ProductoVariante;
import com.pikudo.restaurant.entity.orders.OrderItemModifier;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderServiceType;
import com.pikudo.restaurant.entity.orders.OrderSource;
import com.pikudo.restaurant.entity.orders.TableSession;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.mapper.orders.OrderResponseMapper;
import com.pikudo.restaurant.repository.MesaRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.repository.catalog.ModifierRepository;
import com.pikudo.restaurant.repository.catalog.ProductoVarianteRepository;
import com.pikudo.restaurant.repository.orders.OrderStatusHistoryRepository;
import com.pikudo.restaurant.service.orders.OrderService;
import com.pikudo.restaurant.service.orders.OrderTransitionService;
import com.pikudo.restaurant.service.orders.TableSessionService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final ModifierRepository modifierRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final TableSessionService tableSessionService;
    private final OrderTransitionService transitionService;
    private final OrderResponseMapper mapper;

    @Override
    public Page<OrderResponseDTO> list(
            OrderOperationalStatus operationalStatus,
            OrderPaymentStatus paymentStatus,
            Long mesaId,
            OrderServiceType serviceType,
            OrderSource source,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {
        return pedidoRepository.findAll(filters(operationalStatus, paymentStatus, mesaId, serviceType, source, from, to), pageable)
                .map(mapper::toOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponseDTO create(OrderCreateRequestDTO request) {
        Usuario user = currentUser();
        OrderServiceType serviceType = request.getServiceType();
        OrderSource source = request.getSource() != null ? request.getSource() : defaultSource(serviceType);

        Mesa mesa = null;
        TableSession tableSession = null;
        if (serviceType == OrderServiceType.DINE_IN) {
            tableSession = tableSessionService.ensureOpenSession(request.getMesaId(), request.getTableSessionId(), user);
            mesa = tableSession.getMesa();
        } else if (request.getMesaId() != null) {
            mesa = mesaRepository.findById(request.getMesaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada: " + request.getMesaId()));
        }

        Pedido pedido = new Pedido();
        pedido.setOrderCode(generateCode("ORD"));
        pedido.setTrackingCode(serviceType == OrderServiceType.DELIVERY ? generateCode("TRK") : null);
        pedido.setMesa(mesa);
        pedido.setTableSession(tableSession);
        pedido.setMesero(user);
        pedido.setEstado(EstadoPedido.PENDING);
        pedido.setEstadoOperativo(OrderOperationalStatus.UNREAD);
        pedido.setEstadoPago(OrderPaymentStatus.UNPAID);
        pedido.setServiceType(serviceType);
        pedido.setSource(source);
        pedido.setTipoPedido(toLegacyTipoPedido(serviceType));
        pedido.setDireccion(request.getDireccion());
        pedido.setTelefonoCliente(request.getTelefonoCliente());
        pedido.setObservacionesPedido(request.getObservacionesPedido());
        pedido.setDiscountTotal(BigDecimal.ZERO);
        pedido.setTaxTotal(BigDecimal.ZERO);
        pedido.setDeliveryFee(BigDecimal.ZERO);
        if (serviceType == OrderServiceType.DELIVERY && request.getDireccion() != null && !request.getDireccion().isBlank()) {
            pedido.setUrlMaps("https://www.google.com/maps/search/?api=1&query="
                    + URLEncoder.encode(request.getDireccion(), StandardCharsets.UTF_8));
        }

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();
        for (OrderCreateRequestDTO.Item item : request.getItems()) {
            DetallePedido detalle = buildDetalle(pedido, item);
            total = total.add(detalle.getLineTotal());
            detalles.add(detalle);
        }
        pedido.setSubtotal(total);
        pedido.setTotal(total);
        pedido.setDetalles(detalles);

        Pedido saved = pedidoRepository.save(pedido);
        transitionService.recordInitialStatus(saved, user, "Pedido creado");
        return mapper.toOrderResponse(saved);
    }

    @Override
    public OrderResponseDTO get(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
        return mapper.toOrderResponse(pedido);
    }

    @Override
    @Transactional
    public OrderResponseDTO transition(Long id, OrderStatusChangeRequestDTO request) {
        Usuario user = currentUser();
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
        Pedido transitioned = transitionService.transition(pedido, request.getStatus(), user, request.getReason());
        return mapper.toOrderResponse(transitioned);
    }

    @Override
    public List<OrderStatusHistoryResponseDTO> history(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido no encontrado: " + id);
        }
        return historyRepository.findByPedidoIdOrderByFechaCreacionAscIdAsc(id).stream()
                .map(mapper::toHistoryResponse)
                .toList();
    }

    private DetallePedido buildDetalle(Pedido pedido, OrderCreateRequestDTO.Item item) {
        ProductoVariante variant = productoVarianteRepository.findById(item.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + item.getVariantId()));
        if (Boolean.FALSE.equals(variant.getDisponible()) || Boolean.FALSE.equals(variant.getVisiblePublico())) {
            throw new BusinessException("La variante no esta disponible para venta: " + item.getVariantId());
        }
        Producto producto = variant.getProducto();
        if (producto == null) {
            throw new BusinessException("La variante no tiene producto asociado.");
        }
        if (Boolean.FALSE.equals(producto.getDisponible()) || Boolean.FALSE.equals(producto.getEstado())) {
            throw new BusinessException("El producto no esta disponible para venta: " + producto.getId());
        }

        int itemQuantity = item.getQuantity();
        BigDecimal modifierUnitTotal = BigDecimal.ZERO;
        List<OrderItemModifier> modifiers = new ArrayList<>();
        for (OrderCreateRequestDTO.SelectedModifier selectedModifier : item.getModifiers()) {
            Modifier modifier = modifierRepository.findById(selectedModifier.getModifierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Modificador no encontrado: " + selectedModifier.getModifierId()));
            if (Boolean.FALSE.equals(modifier.getActivo()) || Boolean.FALSE.equals(modifier.getVisiblePublico())) {
                throw new BusinessException("El modificador no esta disponible: " + modifier.getId());
            }
            int modifierQuantity = selectedModifier.getQuantity() != null ? selectedModifier.getQuantity() : 1;
            BigDecimal perUnitExtra = modifier.getPrecioExtra().multiply(BigDecimal.valueOf(modifierQuantity));
            BigDecimal lineExtra = perUnitExtra.multiply(BigDecimal.valueOf(itemQuantity));
            modifierUnitTotal = modifierUnitTotal.add(perUnitExtra);

            modifiers.add(OrderItemModifier.builder()
                    .modifier(modifier)
                    .modifierGroup(modifier.getGroup())
                    .modifierGroupNameSnapshot(modifier.getGroup() != null ? modifier.getGroup().getNombre() : null)
                    .modifierNameSnapshot(modifier.getNombre())
                    .extraPriceSnapshot(modifier.getPrecioExtra())
                    .quantity(modifierQuantity)
                    .totalExtra(lineExtra)
                    .build());
        }

        BigDecimal unitPrice = variant.getPrecioActual().add(modifierUnitTotal);
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemQuantity));
        DetallePedido detalle = DetallePedido.builder()
                .pedido(pedido)
                .producto(producto)
                .variante(variant)
                .cantidad(itemQuantity)
                .precioUnitario(unitPrice)
                .precioUnitarioSnapshot(unitPrice)
                .productoNombreSnapshot(producto.getNombre())
                .varianteNombreSnapshot(variant.getNombre())
                .subtotal(lineTotal)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(lineTotal)
                .observaciones(item.getNotes())
                .modifiers(modifiers)
                .build();
        modifiers.forEach(modifier -> modifier.setDetallePedido(detalle));
        return detalle;
    }

    private Specification<Pedido> filters(
            OrderOperationalStatus operationalStatus,
            OrderPaymentStatus paymentStatus,
            Long mesaId,
            OrderServiceType serviceType,
            OrderSource source,
            LocalDateTime from,
            LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (operationalStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("estadoOperativo"), operationalStatus));
            }
            if (paymentStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("estadoPago"), paymentStatus));
            }
            if (mesaId != null) {
                predicates.add(criteriaBuilder.equal(root.get("mesa").get("id"), mesaId));
            }
            if (serviceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), serviceType));
            }
            if (source != null) {
                predicates.add(criteriaBuilder.equal(root.get("source"), source));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCreacion"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaCreacion"), to));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Usuario currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException("No hay usuario autenticado.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesion no encontrado"));
    }

    private String generateCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private OrderSource defaultSource(OrderServiceType serviceType) {
        return switch (serviceType) {
            case DELIVERY -> OrderSource.WEB;
            case PICKUP -> OrderSource.WALK_IN;
            case DINE_IN -> OrderSource.DINE_IN;
        };
    }

    private String toLegacyTipoPedido(OrderServiceType serviceType) {
        return switch (serviceType) {
            case DELIVERY -> "DELIVERY";
            case PICKUP -> "RECOJO";
            case DINE_IN -> "MESA";
        };
    }
}
