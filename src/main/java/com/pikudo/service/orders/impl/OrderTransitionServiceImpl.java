package com.pikudo.service.orders.impl;

import com.pikudo.entity.EstadoPedido;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.orders.OrderOperationalStatus;
import com.pikudo.entity.orders.OrderStatusHistory;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.orders.OrderStatusHistoryRepository;
import com.pikudo.service.orders.OrderTransitionService;
import com.pikudo.service.orders.TableSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderTransitionServiceImpl implements OrderTransitionService {

    private static final Map<OrderOperationalStatus, Set<OrderOperationalStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(OrderOperationalStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.UNREAD, EnumSet.of(
                OrderOperationalStatus.READ,
                OrderOperationalStatus.ACCEPTED,
                OrderOperationalStatus.REJECTED,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.READ, EnumSet.of(
                OrderOperationalStatus.ACCEPTED,
                OrderOperationalStatus.REJECTED,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.ACCEPTED, EnumSet.of(
                OrderOperationalStatus.IN_PREPARATION,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.IN_PREPARATION, EnumSet.of(
                OrderOperationalStatus.READY,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.READY, EnumSet.of(
                OrderOperationalStatus.ASSIGNED,
                OrderOperationalStatus.DELIVERED,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.ASSIGNED, EnumSet.of(
                OrderOperationalStatus.ON_DELIVERY,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.ON_DELIVERY, EnumSet.of(
                OrderOperationalStatus.NEAR_CUSTOMER,
                OrderOperationalStatus.DELIVERED,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.NEAR_CUSTOMER, EnumSet.of(
                OrderOperationalStatus.DELIVERED,
                OrderOperationalStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.REJECTED, EnumSet.noneOf(OrderOperationalStatus.class));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.DELIVERED, EnumSet.noneOf(OrderOperationalStatus.class));
        ALLOWED_TRANSITIONS.put(OrderOperationalStatus.CANCELLED, EnumSet.noneOf(OrderOperationalStatus.class));
    }

    private final PedidoRepository pedidoRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final TableSessionService tableSessionService;

    @Override
    @Transactional
    public Pedido transition(Pedido pedido, OrderOperationalStatus targetStatus, Usuario changedBy, String reason) {
        OrderOperationalStatus currentStatus = pedido.getEstadoOperativo();
        if (currentStatus == targetStatus) {
            return pedido;
        }
        Set<OrderOperationalStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new BusinessException("Transicion de pedido invalida: " + currentStatus + " -> " + targetStatus);
        }

        pedido.setEstadoOperativo(targetStatus);
        pedido.setEstado(toLegacyStatus(targetStatus, pedido.getEstado()));
        Pedido saved = pedidoRepository.save(pedido);
        saveHistory(saved, currentStatus, targetStatus, changedBy, reason);

        if (targetStatus == OrderOperationalStatus.CANCELLED || targetStatus == OrderOperationalStatus.REJECTED) {
            tableSessionService.closeIfNoOpenOrders(saved.getTableSession(), changedBy);
        }
        return saved;
    }

    @Override
    @Transactional
    public void recordInitialStatus(Pedido pedido, Usuario changedBy, String reason) {
        saveHistory(pedido, null, pedido.getEstadoOperativo(), changedBy, reason);
    }

    private void saveHistory(Pedido pedido,
                             OrderOperationalStatus fromStatus,
                             OrderOperationalStatus toStatus,
                             Usuario changedBy,
                             String reason) {
        historyRepository.save(OrderStatusHistory.builder()
                .pedido(pedido)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .reason(reason)
                .build());
    }

    private EstadoPedido toLegacyStatus(OrderOperationalStatus status, EstadoPedido currentLegacy) {
        return switch (status) {
            case ON_DELIVERY, ASSIGNED, NEAR_CUSTOMER -> EstadoPedido.ON_DELIVERY;
            case DELIVERED -> EstadoPedido.DELIVERED;
            case REJECTED, CANCELLED -> EstadoPedido.CANCELLED;
            default -> currentLegacy == EstadoPedido.PAID ? EstadoPedido.PAID : EstadoPedido.PENDING;
        };
    }
}
