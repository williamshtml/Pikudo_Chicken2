package com.pikudo.mapper.orders;

import com.pikudo.dto.orders.OrderItemModifierResponseDTO;
import com.pikudo.dto.orders.OrderItemResponseDTO;
import com.pikudo.dto.orders.OrderDiscountResponseDTO;
import com.pikudo.dto.orders.OrderPaymentSummaryDTO;
import com.pikudo.dto.orders.OrderResponseDTO;
import com.pikudo.dto.orders.OrderStatusHistoryResponseDTO;
import com.pikudo.dto.orders.TableSessionResponseDTO;
import com.pikudo.entity.DetallePedido;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.orders.OrderDiscount;
import com.pikudo.entity.orders.OrderPaymentStatus;
import com.pikudo.entity.orders.OrderPaymentStatusType;
import com.pikudo.entity.orders.OrderItemModifier;
import com.pikudo.entity.orders.OrderStatusHistory;
import com.pikudo.entity.orders.TableSession;
import com.pikudo.repository.orders.OrderDiscountRepository;
import com.pikudo.repository.orders.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderResponseMapper {

    private final OrderPaymentRepository paymentRepository;
    private final OrderDiscountRepository discountRepository;

    public OrderResponseDTO toOrderResponse(Pedido pedido) {
        OrderPaymentSummaryDTO paymentSummary = toPaymentSummary(pedido);
        return OrderResponseDTO.builder()
                .id(pedido.getId())
                .orderCode(pedido.getOrderCode())
                .trackingCode(pedido.getTrackingCode())
                .mesaId(pedido.getMesa() != null ? pedido.getMesa().getId() : null)
                .mesaNumero(pedido.getMesa() != null ? pedido.getMesa().getNumero() : null)
                .tableSessionId(pedido.getTableSession() != null ? pedido.getTableSession().getId() : null)
                .estadoOperativo(pedido.getEstadoOperativo())
                .estadoPago(pedido.getEstadoPago())
                .serviceType(pedido.getServiceType())
                .source(pedido.getSource())
                .subtotal(pedido.getSubtotal())
                .discountTotal(pedido.getDiscountTotal())
                .taxTotal(pedido.getTaxTotal())
                .deliveryFee(pedido.getDeliveryFee())
                .total(pedido.getTotal())
                .direccion(pedido.getDireccion())
                .telefonoCliente(pedido.getTelefonoCliente())
                .observacionesPedido(pedido.getObservacionesPedido())
                .fechaCreacion(pedido.getFechaCreacion())
                .paymentSummary(paymentSummary)
                .discounts(pedido.getId() == null ? List.of() : discountRepository.findByPedidoIdOrderByFechaCreacionAscIdAsc(pedido.getId()).stream()
                        .map(this::toDiscountResponse)
                        .toList())
                .items(pedido.getDetalles() == null ? List.of() : pedido.getDetalles().stream()
                        .map(this::toItemResponse)
                        .toList())
                .build();
    }

    private OrderPaymentSummaryDTO toPaymentSummary(Pedido pedido) {
        BigDecimal paid = pedido.getId() == null ? BigDecimal.ZERO :
                paymentRepository.sumByPedidoIdAndStatus(pedido.getId(), OrderPaymentStatusType.CONFIRMED);
        paid = paid != null ? paid : BigDecimal.ZERO;
        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        BigDecimal pending = total.subtract(paid).max(BigDecimal.ZERO);
        OrderPaymentStatus status = paid.compareTo(BigDecimal.ZERO) <= 0
                ? OrderPaymentStatus.UNPAID
                : (paid.compareTo(total) >= 0 ? OrderPaymentStatus.PAID : OrderPaymentStatus.PARTIALLY_PAID);
        return OrderPaymentSummaryDTO.builder()
                .orderId(pedido.getId())
                .total(total)
                .paidAmount(paid)
                .pendingAmount(pending)
                .paymentStatus(status)
                .build();
    }

    public OrderItemResponseDTO toItemResponse(DetallePedido detalle) {
        return OrderItemResponseDTO.builder()
                .id(detalle.getId())
                .productId(detalle.getProducto() != null ? detalle.getProducto().getId() : null)
                .variantId(detalle.getVariante() != null ? detalle.getVariante().getId() : null)
                .productName(detalle.getProductoNombreSnapshot())
                .variantName(detalle.getVarianteNombreSnapshot())
                .quantity(detalle.getCantidad())
                .unitPrice(detalle.getPrecioUnitarioSnapshot())
                .discountAmount(detalle.getDiscountAmount())
                .taxAmount(detalle.getTaxAmount())
                .lineTotal(detalle.getLineTotal())
                .notes(detalle.getObservaciones())
                .modifiers(detalle.getModifiers() == null ? List.of() : detalle.getModifiers().stream()
                        .map(this::toModifierResponse)
                        .toList())
                .build();
    }

    public OrderItemModifierResponseDTO toModifierResponse(OrderItemModifier modifier) {
        return OrderItemModifierResponseDTO.builder()
                .id(modifier.getId())
                .modifierId(modifier.getModifier() != null ? modifier.getModifier().getId() : null)
                .modifierGroupId(modifier.getModifierGroup() != null ? modifier.getModifierGroup().getId() : null)
                .modifierGroupName(modifier.getModifierGroupNameSnapshot())
                .modifierName(modifier.getModifierNameSnapshot())
                .quantity(modifier.getQuantity())
                .extraPrice(modifier.getExtraPriceSnapshot())
                .totalExtra(modifier.getTotalExtra())
                .build();
    }

    public OrderStatusHistoryResponseDTO toHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponseDTO.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedByUsername(history.getChangedBy() != null ? history.getChangedBy().getUsername() : null)
                .reason(history.getReason())
                .fechaCreacion(history.getFechaCreacion())
                .build();
    }

    public OrderDiscountResponseDTO toDiscountResponse(OrderDiscount discount) {
        return OrderDiscountResponseDTO.builder()
                .id(discount.getId())
                .orderId(discount.getPedido() != null ? discount.getPedido().getId() : null)
                .detailId(discount.getDetallePedido() != null ? discount.getDetallePedido().getId() : null)
                .discountType(discount.getDiscountType())
                .requestedValue(discount.getRequestedValue())
                .calculatedAmount(discount.getCalculatedAmount())
                .reason(discount.getReason())
                .authorizedByUsername(discount.getAuthorizedBy() != null ? discount.getAuthorizedBy().getUsername() : null)
                .status(discount.getStatus())
                .fechaCreacion(discount.getFechaCreacion())
                .build();
    }

    public TableSessionResponseDTO toTableSessionResponse(TableSession session) {
        return TableSessionResponseDTO.builder()
                .id(session.getId())
                .mesaId(session.getMesa() != null ? session.getMesa().getId() : null)
                .mesaNumero(session.getMesa() != null ? session.getMesa().getNumero() : null)
                .status(session.getStatus())
                .guestCount(session.getGuestCount())
                .notes(session.getNotes())
                .openedByUsername(session.getOpenedBy() != null ? session.getOpenedBy().getUsername() : null)
                .closedByUsername(session.getClosedBy() != null ? session.getClosedBy().getUsername() : null)
                .openedAt(session.getOpenedAt())
                .closedAt(session.getClosedAt())
                .build();
    }
}
