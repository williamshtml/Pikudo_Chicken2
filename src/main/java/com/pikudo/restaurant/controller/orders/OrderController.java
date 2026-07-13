package com.pikudo.restaurant.controller.orders;

import com.pikudo.restaurant.dto.orders.OrderCreateRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderDiscountRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderDiscountResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentSummaryDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentVoidRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderStatusChangeRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderStatusHistoryResponseDTO;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderServiceType;
import com.pikudo.restaurant.entity.orders.OrderSource;
import com.pikudo.restaurant.service.orders.OrderService;
import com.pikudo.restaurant.service.orders.OrderPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO', 'MOTORIZADO')")
    public Page<OrderResponseDTO> list(
            @RequestParam(required = false) OrderOperationalStatus operationalStatus,
            @RequestParam(required = false) OrderPaymentStatus paymentStatus,
            @RequestParam(required = false) Long mesaId,
            @RequestParam(required = false) OrderServiceType serviceType,
            @RequestParam(required = false) OrderSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return orderService.list(operationalStatus, paymentStatus, mesaId, serviceType, source, from, to, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderCreateRequestDTO request) {
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO', 'MOTORIZADO')")
    public OrderResponseDTO get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public OrderResponseDTO read(@PathVariable Long id) {
        OrderStatusChangeRequestDTO request = new OrderStatusChangeRequestDTO();
        request.setStatus(OrderOperationalStatus.READ);
        request.setReason("Pedido leido");
        return orderService.transition(id, request);
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public OrderResponseDTO accept(@PathVariable Long id) {
        OrderStatusChangeRequestDTO request = new OrderStatusChangeRequestDTO();
        request.setStatus(OrderOperationalStatus.ACCEPTED);
        request.setReason("Pedido aceptado");
        return orderService.transition(id, request);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public OrderResponseDTO reject(@PathVariable Long id, @RequestBody(required = false) OrderStatusChangeRequestDTO request) {
        OrderStatusChangeRequestDTO effectiveRequest = request != null ? request : new OrderStatusChangeRequestDTO();
        effectiveRequest.setStatus(OrderOperationalStatus.REJECTED);
        if (effectiveRequest.getReason() == null) {
            effectiveRequest.setReason("Pedido rechazado");
        }
        return orderService.transition(id, effectiveRequest);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO', 'MOTORIZADO')")
    public OrderResponseDTO changeStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusChangeRequestDTO request) {
        return orderService.transition(id, request);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public List<OrderStatusHistoryResponseDTO> history(@PathVariable Long id) {
        return orderService.history(id);
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    public ResponseEntity<OrderPaymentResponseDTO> addPayment(@PathVariable Long id,
                                                              @Valid @RequestBody OrderPaymentRequestDTO request) {
        return new ResponseEntity<>(orderPaymentService.addPayment(id, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    public List<OrderPaymentResponseDTO> listPayments(@PathVariable Long id) {
        return orderPaymentService.listPayments(id);
    }

    @GetMapping("/{id}/payment-summary")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public OrderPaymentSummaryDTO paymentSummary(@PathVariable Long id) {
        return orderPaymentService.getSummary(id);
    }

    @PostMapping("/{id}/payments/{paymentId}/void")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    public OrderPaymentResponseDTO voidPayment(@PathVariable Long id,
                                               @PathVariable Long paymentId,
                                               @Valid @RequestBody OrderPaymentVoidRequestDTO request) {
        return orderPaymentService.voidPayment(id, paymentId, request);
    }

    @PostMapping("/{id}/discounts")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    public ResponseEntity<OrderDiscountResponseDTO> applyDiscount(@PathVariable Long id,
                                                                  @Valid @RequestBody OrderDiscountRequestDTO request) {
        return new ResponseEntity<>(orderPaymentService.applyDiscount(id, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/discounts")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public List<OrderDiscountResponseDTO> listDiscounts(@PathVariable Long id) {
        return orderPaymentService.listDiscounts(id);
    }
}
