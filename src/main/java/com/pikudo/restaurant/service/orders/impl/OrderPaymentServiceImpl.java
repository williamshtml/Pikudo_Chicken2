package com.pikudo.restaurant.service.orders.impl;

import com.pikudo.restaurant.dto.orders.OrderDiscountRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderDiscountResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentSummaryDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentVoidRequestDTO;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.caja.Caja;
import com.pikudo.restaurant.entity.caja.MetodoPago;
import com.pikudo.restaurant.entity.orders.OrderDiscount;
import com.pikudo.restaurant.entity.orders.OrderDiscountStatus;
import com.pikudo.restaurant.entity.orders.OrderDiscountType;
import com.pikudo.restaurant.entity.orders.OrderPayment;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatusType;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.repository.CajaRepository;
import com.pikudo.restaurant.repository.MetodoPagoRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.repository.orders.OrderDiscountRepository;
import com.pikudo.restaurant.repository.orders.OrderPaymentRepository;
import com.pikudo.restaurant.service.orders.OrderPaymentService;
import com.pikudo.restaurant.service.orders.TableSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderPaymentRepository paymentRepository;
    private final OrderDiscountRepository discountRepository;
    private final PedidoRepository pedidoRepository;
    private final CajaRepository cajaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TableSessionService tableSessionService;

    @Override
    @Transactional
    public OrderPaymentResponseDTO addPayment(Long orderId, OrderPaymentRequestDTO request) {
        Pedido pedido = findOrder(orderId);
        if (pedido.getEstadoPago() == OrderPaymentStatus.PAID) {
            throw new BusinessException("El pedido ya esta pagado.");
        }
        Caja caja = cajaRepository.findByEstado("ABIERTA")
                .orElseThrow(() -> new BusinessException("No hay caja abierta para registrar el pago."));
        MetodoPago metodoPago = metodoPagoRepository.findByNombreIgnoreCase(request.getMetodoPago())
                .orElseThrow(() -> new BusinessException("Metodo de pago no reconocido: " + request.getMetodoPago()));
        if (!Boolean.TRUE.equals(metodoPago.getActivo())) {
            throw new BusinessException("El metodo de pago esta deshabilitado: " + request.getMetodoPago());
        }

        OrderPaymentSummaryDTO summary = summarize(pedido);
        if (request.getMonto().compareTo(summary.getPendingAmount()) > 0) {
            throw new BusinessException("El pago excede el saldo pendiente del pedido.");
        }

        OrderPayment payment = OrderPayment.builder()
                .pedido(pedido)
                .caja(caja)
                .metodoPago(metodoPago)
                .monto(request.getMonto())
                .status(OrderPaymentStatusType.CONFIRMED)
                .externalReference(request.getExternalReference())
                .notes(request.getNotes())
                .receivedBy(currentUser())
                .build();
        OrderPayment saved = paymentRepository.save(payment);
        refreshPaymentStatus(pedido);
        return toPaymentResponse(saved);
    }

    @Override
    public List<OrderPaymentResponseDTO> listPayments(Long orderId) {
        if (!pedidoRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Pedido no encontrado: " + orderId);
        }
        return paymentRepository.findByPedidoIdOrderByFechaCreacionAscIdAsc(orderId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Override
    public OrderPaymentSummaryDTO getSummary(Long orderId) {
        return summarize(findOrder(orderId));
    }

    @Override
    public OrderPaymentSummaryDTO summarize(Pedido pedido) {
        BigDecimal paid = paymentRepository.sumByPedidoIdAndStatus(pedido.getId(), OrderPaymentStatusType.CONFIRMED);
        paid = paid != null ? paid : BigDecimal.ZERO;
        BigDecimal total = money(pedido.getTotal());
        BigDecimal pending = total.subtract(paid).max(BigDecimal.ZERO);
        return OrderPaymentSummaryDTO.builder()
                .orderId(pedido.getId())
                .total(total)
                .paidAmount(paid)
                .pendingAmount(pending)
                .paymentStatus(resolvePaymentStatus(total, paid))
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentResponseDTO voidPayment(Long orderId, Long paymentId, OrderPaymentVoidRequestDTO request) {
        Pedido pedido = findOrder(orderId);
        OrderPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + paymentId));
        if (!payment.getPedido().getId().equals(orderId)) {
            throw new BusinessException("El pago no pertenece al pedido indicado.");
        }
        if (payment.getStatus() != OrderPaymentStatusType.CONFIRMED) {
            throw new BusinessException("Solo se pueden anular pagos confirmados.");
        }
        Usuario user = currentUser();
        payment.setStatus(OrderPaymentStatusType.VOIDED);
        payment.setVoidedBy(user);
        payment.setVoidReason(request.getReason());
        payment.setVoidedAt(LocalDateTime.now());
        OrderPayment saved = paymentRepository.save(payment);
        refreshPaymentStatus(pedido);
        return toPaymentResponse(saved);
    }

    @Override
    @Transactional
    public OrderDiscountResponseDTO applyDiscount(Long orderId, OrderDiscountRequestDTO request) {
        Pedido pedido = findOrder(orderId);
        if (pedido.getEstadoPago() == OrderPaymentStatus.PAID) {
            throw new BusinessException("No se puede aplicar descuento a un pedido ya pagado.");
        }

        DetallePedido targetDetail = null;
        BigDecimal discountBase;
        if (request.getDetailId() != null) {
            targetDetail = pedido.getDetalles().stream()
                    .filter(detalle -> detalle.getId().equals(request.getDetailId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado en el pedido: " + request.getDetailId()));
            discountBase = money(targetDetail.getLineTotal());
        } else {
            discountBase = money(pedido.getTotal());
        }

        BigDecimal discountAmount = calculateDiscount(request.getDiscountType(), request.getValue(), discountBase);
        BigDecimal paid = paymentRepository.sumByPedidoIdAndStatus(orderId, OrderPaymentStatusType.CONFIRMED);
        BigDecimal newTotal = money(pedido.getTotal()).subtract(discountAmount);
        if (paid != null && paid.compareTo(newTotal) > 0) {
            throw new BusinessException("El descuento dejaria el total por debajo del monto ya cobrado.");
        }

        applyDiscountToAmounts(pedido, targetDetail, discountAmount);
        OrderDiscount discount = OrderDiscount.builder()
                .pedido(pedido)
                .detallePedido(targetDetail)
                .discountType(request.getDiscountType())
                .requestedValue(request.getValue())
                .calculatedAmount(discountAmount)
                .reason(request.getReason())
                .authorizedBy(currentUser())
                .status(OrderDiscountStatus.APPLIED)
                .build();
        pedidoRepository.save(pedido);
        OrderDiscount saved = discountRepository.save(discount);
        refreshPaymentStatus(pedido);
        return toDiscountResponse(saved);
    }

    @Override
    public List<OrderDiscountResponseDTO> listDiscounts(Long orderId) {
        if (!pedidoRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Pedido no encontrado: " + orderId);
        }
        return discountRepository.findByPedidoIdOrderByFechaCreacionAscIdAsc(orderId).stream()
                .map(this::toDiscountResponse)
                .toList();
    }

    @Override
    @Transactional
    public void refreshPaymentStatus(Pedido pedido) {
        OrderPaymentSummaryDTO summary = summarize(pedido);
        pedido.setEstadoPago(summary.getPaymentStatus());
        pedidoRepository.save(pedido);
        if (summary.getPaymentStatus() == OrderPaymentStatus.PAID) {
            tableSessionService.closeIfNoOpenOrders(pedido.getTableSession(), null);
        }
    }

    private Pedido findOrder(Long orderId) {
        return pedidoRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));
    }

    private OrderPaymentStatus resolvePaymentStatus(BigDecimal total, BigDecimal paid) {
        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            return OrderPaymentStatus.UNPAID;
        }
        if (paid.compareTo(total) >= 0) {
            return OrderPaymentStatus.PAID;
        }
        return OrderPaymentStatus.PARTIALLY_PAID;
    }

    private BigDecimal calculateDiscount(OrderDiscountType type, BigDecimal value, BigDecimal base) {
        BigDecimal amount = switch (type) {
            case MANUAL_AMOUNT -> value;
            case MANUAL_PERCENT -> {
                if (value.compareTo(new BigDecimal("100")) > 0) {
                    throw new BusinessException("El porcentaje de descuento no puede superar 100.");
                }
                yield base.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        };
        if (amount.compareTo(base) > 0) {
            throw new BusinessException("El descuento no puede superar el monto base.");
        }
        return money(amount);
    }

    private void applyDiscountToAmounts(Pedido pedido, DetallePedido targetDetail, BigDecimal discountAmount) {
        if (targetDetail != null) {
            targetDetail.setDiscountAmount(money(targetDetail.getDiscountAmount()).add(discountAmount));
            targetDetail.setLineTotal(money(targetDetail.getLineTotal()).subtract(discountAmount));
            targetDetail.setSubtotal(targetDetail.getLineTotal());
        }
        pedido.setDiscountTotal(money(pedido.getDiscountTotal()).add(discountAmount));
        pedido.setTotal(money(pedido.getTotal()).subtract(discountAmount));
    }

    private OrderPaymentResponseDTO toPaymentResponse(OrderPayment payment) {
        return OrderPaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getPedido().getId())
                .cajaId(payment.getCaja().getId())
                .metodoPago(payment.getMetodoPago().getNombre())
                .metodoPagoTipo(payment.getMetodoPago().getTipo())
                .monto(payment.getMonto())
                .status(payment.getStatus())
                .externalReference(payment.getExternalReference())
                .notes(payment.getNotes())
                .receivedByUsername(payment.getReceivedBy() != null ? payment.getReceivedBy().getUsername() : null)
                .voidedByUsername(payment.getVoidedBy() != null ? payment.getVoidedBy().getUsername() : null)
                .voidReason(payment.getVoidReason())
                .fechaCreacion(payment.getFechaCreacion())
                .voidedAt(payment.getVoidedAt())
                .build();
    }

    private OrderDiscountResponseDTO toDiscountResponse(OrderDiscount discount) {
        return OrderDiscountResponseDTO.builder()
                .id(discount.getId())
                .orderId(discount.getPedido().getId())
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

    private Usuario currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException("No hay usuario autenticado.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesion no encontrado"));
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
